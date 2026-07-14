import { test, expect } from '@playwright/test'
import { DateTime } from 'luxon'

const apiBase = process.env.E2E_API_BASE_URL ?? 'http://localhost:8081'

test('admin creates and publishes an offer visible through public discovery', async ({ page, request }) => {
  const unique = `${Date.now()}`
  const providerName = `E2E Piknik ${unique}`
  const offerName = `E2E családi műhely ${unique}`
  const now = DateTime.now().setZone('Europe/Budapest').plus({ days: 2 }).startOf('hour')
  const local = (hours: number) => now.plus({ hours }).toFormat("yyyy-MM-dd'T'HH:mm")

  await page.goto('/bejelentkezes')
  await page.getByLabel('E-mail-cím').fill(process.env.E2E_ADMIN_EMAIL ?? 'admin@ugorjbe.local')
  await page.getByLabel('Jelszó').fill(process.env.E2E_ADMIN_PASSWORD ?? 'UgorjBeAdmin123!')
  await page.getByRole('button', { name: /Belépés a programkezelőbe/ }).click()
  await expect(page.getByRole('heading', { name: /Mire készülnek/ })).toBeVisible()
  await page.screenshot({ path: 'test-results/admin-dashboard.png', fullPage: true })

  await page.getByRole('link', { name: /Új szolgáltató/ }).first().click()
  await page.getByLabel('Név').fill(providerName)
  await page.getByLabel('Rövid leírás').fill('E2E családi programhelyszín.')
  await page.getByLabel('Részletes bemutatkozás').fill('Automatizált böngészőteszttel létrehozott családi programhelyszín.')
  await page.getByLabel('Irányítószám').fill('1137')
  await page.getByLabel('Város').fill('Budapest')
  await page.getByLabel('Utca, házszám').fill('Pozsonyi út 12.')
  await page.getByLabel('Szélesség').fill('47.5182')
  await page.getByLabel('Hosszúság').fill('19.0504')
  await page.getByRole('button', { name: 'Szolgáltató létrehozása' }).click()
  await expect(page.getByRole('heading', { name: providerName })).toBeVisible()

  await page.getByRole('link', { name: 'Programok' }).click()
  await page.getByRole('link', { name: '+ Új program' }).click()
  await page.getByLabel('Szolgáltató').selectOption({ label: providerName })
  await page.getByLabel('Program neve').fill(offerName)
  await page.getByLabel('Részletes leírás').fill('Valódi API-integrációt ellenőrző, foglalható családi műhelyprogram.')
  await page.getByLabel('Irányítószám').fill('1137')
  await page.getByLabel('Utca, házszám').fill('Pozsonyi út 12.')
  await page.getByLabel('Szélesség').fill('47.5182')
  await page.getByLabel('Hosszúság').fill('19.0504')
  await page.getByLabel('Kezdés').fill(local(3))
  await page.getByLabel('Befejezés').fill(local(5))
  await page.getByLabel('Foglalási határidő').fill(local(2))
  await page.getByLabel('Lemondási határidő').fill(local(1))
  await page.getByLabel('Minimum életkor').fill('4')
  await page.getByLabel('Maximum életkor').fill('12')
  await page.getByLabel('Összes férőhely').fill('12')
  await page.getByLabel('Eredeti egységár').fill('6000')
  await page.getByLabel('Kedvezményes egységár').fill('3900')
  await page.getByRole('button', { name: 'Piszkozat létrehozása' }).click()
  await expect(page.getByText('Piszkozat', { exact: true }).first()).toBeVisible()
  await page.getByRole('button', { name: 'Közzététel' }).click()
  await page.getByRole('button', { name: 'Igen, közzéteszem' }).click()
  await expect(page.getByText('Közzétett', { exact: true }).first()).toBeVisible()
  await page.screenshot({ path: 'test-results/admin-published-offer.png', fullPage: true })

  const startsFromUtc = encodeURIComponent(now.minus({ hours: 1 }).toUTC().toISO()!)
  const startsToUtc = encodeURIComponent(now.plus({ days: 1 }).toUTC().toISO()!)
  const response = await request.get(`${apiBase}/api/offers/map?south=47.40&west=18.90&north=47.60&east=19.20&q=${encodeURIComponent(offerName)}&startsFromUtc=${startsFromUtc}&startsToUtc=${startsToUtc}&limit=20`)
  expect(response.ok()).toBeTruthy()
  const envelope = await response.json() as { items: { title: string }[] }
  expect(envelope.items.some((item) => item.title === offerName)).toBeTruthy()
})
