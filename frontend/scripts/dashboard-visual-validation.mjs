import { mkdir, writeFile } from "node:fs/promises";
import { Builder, By, until } from "selenium-webdriver";
import firefox from "selenium-webdriver/firefox.js";

const baseUrl = process.env.HACKGOV_FRONTEND_URL || "http://127.0.0.1:5173";
const outputDir = process.env.HACKGOV_SCREENSHOT_DIR || "/tmp/hackgov-dashboard-visual";
const driverPath = process.env.GECKODRIVER_PATH || "/tmp/geckodriver";
const options = new firefox.Options().addArguments("-headless");
const service = new firefox.ServiceBuilder(driverPath);
const driver = await new Builder().forBrowser("firefox").setFirefoxOptions(options)
  .setFirefoxService(service).build();

async function screenshot(name) {
  await writeFile(`${outputDir}/${name}.png`, await driver.takeScreenshot(), "base64");
}

async function assertDashboard() {
  await driver.wait(until.elementLocated(By.css(".dashboard .dash-hero")), 15000);
  await driver.wait(async () => (await driver.findElement(By.css("body")).getText()).includes("Favoritos"), 15000);
  const body = await driver.findElement(By.css("body")).getText();
  for (const expected of ["Funcionários", "Setores", "Cargos", "Suas tarefas", "Ver processos", "Gerenciar Tarefas"]) {
    if (!body.includes(expected)) throw new Error(`Texto obrigatório ausente no dashboard: ${expected}`);
  }
  const stats = await driver.findElements(By.css(".stats-grid .stat-card"));
  if (stats.length !== 4) throw new Error(`Esperados 4 indicadores, encontrados ${stats.length}`);
  const days = await driver.findElements(By.css(".cal-grid .cal-dia"));
  if (days.length !== 0 && days.length !== 35 && days.length !== 42) {
    throw new Error(`Grade mensal inválida: ${days.length} dias`);
  }
  const apiStatus = await driver.executeAsyncScript(`
    const done = arguments[arguments.length - 1];
    const token = localStorage.getItem('hackgov.accessToken');
    fetch('/api/dashboard', { headers: { Authorization: 'Bearer ' + token } })
      .then((response) => done(response.status)).catch(() => done(0));
  `);
  if (apiStatus !== 200) throw new Error(`GET /api/dashboard retornou ${apiStatus}`);
}

try {
  await mkdir(outputDir, { recursive: true });
  await driver.manage().setTimeouts({ implicit: 3000, pageLoad: 20000, script: 10000 });
  await driver.manage().window().setRect({ width: 1440, height: 1050 });
  await driver.get(`${baseUrl}/login`);
  await driver.findElement(By.id("email")).sendKeys("admin@admin.com");
  await driver.findElement(By.id("password")).sendKeys("senha123");
  await driver.findElement(By.css("#loginForm button[type=submit]")).click();
  await driver.wait(until.urlContains("/dashboard"), 15000);
  await assertDashboard();
  await screenshot("dashboard-desktop");
  process.stdout.write("OK dashboard desktop 1440x1050\n");

  await driver.manage().window().setRect({ width: 390, height: 844 });
  await driver.get(`${baseUrl}/dashboard`);
  await assertDashboard();
  await screenshot("dashboard-mobile");
  process.stdout.write("OK dashboard mobile 390x844\n");
  process.stdout.write(`Screenshots: ${outputDir}\n`);
} finally {
  await driver.quit();
}
