FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY backend/Directory.Build.props backend/
COPY backend/src/UgorjBe.Domain/UgorjBe.Domain.csproj backend/src/UgorjBe.Domain/
COPY backend/src/UgorjBe.Application/UgorjBe.Application.csproj backend/src/UgorjBe.Application/
COPY backend/src/UgorjBe.Infrastructure/UgorjBe.Infrastructure.csproj backend/src/UgorjBe.Infrastructure/
COPY backend/src/UgorjBe.Api/UgorjBe.Api.csproj backend/src/UgorjBe.Api/
RUN dotnet restore backend/src/UgorjBe.Api/UgorjBe.Api.csproj
COPY backend/ backend/
RUN dotnet publish backend/src/UgorjBe.Api/UgorjBe.Api.csproj -c Release --no-restore -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS final
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
EXPOSE 8080
ENV ASPNETCORE_URLS=http://+:8080
COPY --from=build /app/publish .
HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=5 CMD curl --fail --silent http://localhost:8080/health || exit 1
ENTRYPOINT ["dotnet", "UgorjBe.Api.dll"]
