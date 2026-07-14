using System.Text.Json.Serialization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.OpenApi.Models;
using UgorjBe.Api.Infrastructure;
using UgorjBe.Application;
using UgorjBe.Infrastructure;
using UgorjBe.Infrastructure.Persistence;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddInfrastructure(builder.Configuration);
var corsOrigins = builder.Configuration.GetSection("Cors:AllowedOrigins").Get<string[]>() ?? ["http://localhost:5173"];
builder.Services.AddCors(options => options.AddPolicy("AdminDevelopment", policy => policy
    .WithOrigins(corsOrigins)
    .WithMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    .WithHeaders("Authorization", "Content-Type")));
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter(allowIntegerValues: false));
        options.JsonSerializerOptions.Converters.Add(new UtcDateTimeOffsetConverter());
        options.JsonSerializerOptions.Converters.Add(new NullableUtcDateTimeOffsetConverter());
    });
builder.Services.Configure<ApiBehaviorOptions>(options =>
{
    options.InvalidModelStateResponseFactory = context =>
    {
        var errors = context.ModelState
            .Where(x => x.Value is { Errors.Count: > 0 })
            .ToDictionary(
                x => string.IsNullOrWhiteSpace(x.Key) ? "request" : char.ToLowerInvariant(x.Key[0]) + x.Key[1..],
                x => x.Value!.Errors.Select(error => string.IsNullOrWhiteSpace(error.ErrorMessage) ? "A mező értéke érvénytelen." : error.ErrorMessage).ToArray());
        var payload = ProblemResponse.Create(
            context.HttpContext,
            400,
            "VALIDATION_FAILED",
            "A kérés érvénytelen.",
            "Egy vagy több mező hibás.",
            new Dictionary<string, object?> { ["errors"] = errors });
        return new ObjectResult(payload)
        {
            StatusCode = 400,
            ContentTypes = { "application/problem+json" }
        };
    };
});
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo { Title = "UgorjBe API", Version = "v1" });
    options.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        Description = "UgorjBe customer access token"
    });
    options.OperationFilter<AuthorizeOperationFilter>();
    options.OperationFilter<ProblemResponsesOperationFilter>();
});

var app = builder.Build();
app.UseMiddleware<ExceptionMiddleware>();
app.UseStatusCodePages(async statusCodeContext =>
{
    var response = statusCodeContext.HttpContext.Response;
    if (response.HasStarted || response.StatusCode < 400 || response.ContentLength is > 0) return;
    var code = response.StatusCode == 401 ? "AUTH_REQUIRED" : "VALIDATION_FAILED";
    var payload = ProblemResponse.Create(
        statusCodeContext.HttpContext,
        response.StatusCode,
        code,
        "A kérés nem teljesíthető.",
        "Ellenőrizd a kérés útvonalát és tartalmát.");
    response.ContentType = "application/problem+json";
    await response.WriteAsJsonAsync(payload, statusCodeContext.HttpContext.RequestAborted);
});

app.UseSwagger();
if (app.Environment.IsDevelopment())
{
    app.UseSwaggerUI();
}

app.UseCors("AdminDevelopment");
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();
await DatabaseBootstrapper.InitializeAsync(app.Services, app.Configuration, app.Lifetime.ApplicationStopping);
await app.RunAsync();

public partial class Program;
