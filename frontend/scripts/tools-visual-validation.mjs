import { mkdir, writeFile } from "node:fs/promises";
import { Builder, By, until } from "selenium-webdriver";
import firefox from "selenium-webdriver/firefox.js";

const baseUrl = process.env.HACKGOV_FRONTEND_URL || "http://127.0.0.1:5173";
const outputDir = process.env.HACKGOV_SCREENSHOT_DIR || "/tmp/hackgov-tools-visual";
const driverPath = process.env.GECKODRIVER_PATH || "/tmp/geckodriver";
const driver = await new Builder().forBrowser("firefox")
  .setFirefoxOptions(new firefox.Options().addArguments("-headless"))
  .setFirefoxService(new firefox.ServiceBuilder(driverPath)).build();

async function shot(name) { await writeFile(`${outputDir}/${name}.png`, await driver.takeScreenshot(), "base64"); }
async function assertText(text) {
  await driver.wait(async () => (await driver.findElement(By.css("body")).getText()).includes(text), 15000);
}

try {
  await mkdir(outputDir, { recursive: true });
  await driver.manage().setTimeouts({ implicit: 2500, pageLoad: 20000, script: 10000 });
  await driver.manage().window().setRect({ width: 1440, height: 1050 });
  await driver.get(`${baseUrl}/login`);
  await driver.findElement(By.id("email")).sendKeys("admin@admin.com");
  await driver.findElement(By.id("password")).sendKeys("senha123");
  await driver.findElement(By.css("#loginForm button[type=submit]")).click();
  await driver.wait(until.urlContains("/dashboard"), 15000);
  await driver.get(`${baseUrl}/ferramentas`);
  await assertText("Ferramentas disponíveis");
  await assertText("Todas as ferramentas");
  const cards = await driver.findElements(By.css(".ferr-card"));
  if (cards.length !== 17) throw new Error(`Esperadas 17 ferramentas, encontradas ${cards.length}`);
  await shot("tools-desktop");

  const categoryFlow = await driver.executeAsyncScript(`
    const done = arguments[arguments.length - 1];
    const token = localStorage.getItem('hackgov.accessToken');
    const headers = { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' };
    let category = null;
    (async () => {
      let result = { ok: true };
      try {
        let response = await fetch('/api/tool-categories', { method: 'POST', headers, body: JSON.stringify({ name: 'Validacao Visual ' + Date.now(), description: '', icon: 'bi-folder-fill', order: 99, active: true }) });
        if (!response.ok) throw new Error('criacao da pasta: ' + response.status);
        category = await response.json();
        response = await fetch('/api/tools/agenda/category', { method: 'PATCH', headers, body: JSON.stringify({ categoryId: category.id }) });
        if (!response.ok) throw new Error('associacao da pasta: ' + response.status);
        const assigned = await response.json();
        if (assigned.categoryId !== category.id) throw new Error('associacao nao persistida');
      } catch (error) {
        result = { ok: false, error: error.message };
      } finally {
        if (category) {
          await fetch('/api/tools/agenda/category', { method: 'PATCH', headers, body: JSON.stringify({ categoryId: null }) });
          await fetch('/api/tool-categories/' + category.id, { method: 'DELETE', headers });
        }
        done(result);
      }
    })();
  `);
  if (!categoryFlow.ok) throw new Error(`Fluxo de pastas falhou: ${categoryFlow.error}`);

  await driver.findElement(By.css(".ferr-manage-button")).click();
  await driver.wait(until.elementLocated(By.css(".ferr-manager-panel")), 5000);
  await assertText("Configuração das ferramentas");
  await shot("tools-manager");

  const agendaCard = await driver.findElement(By.xpath("//article[contains(@class,'ferr-card')][.//*[normalize-space()='Agenda Municipal']]"));
  await agendaCard.findElement(By.css(".ferr-permission-button")).click();
  await driver.wait(until.elementLocated(By.css(".ferr-permission-modal")), 5000);
  await assertText("Política da ferramenta");
  await assertText("Adicionar regra de acesso");
  await shot("tools-access-modal");
  await driver.findElement(By.css(".ferr-permission-modal .btn-close")).click();

  await driver.manage().window().setRect({ width: 390, height: 844 });
  await driver.get(`${baseUrl}/ferramentas`);
  await assertText("Ferramentas disponíveis");
  await shot("tools-mobile");

  await driver.executeScript("localStorage.clear()");
  await driver.manage().window().setRect({ width: 1440, height: 1050 });
  await driver.get(`${baseUrl}/login`);
  await driver.findElement(By.id("email")).sendKeys("joao@sp.gov.br");
  await driver.findElement(By.id("password")).sendKeys("senha123");
  await driver.findElement(By.css("#loginForm button[type=submit]")).click();
  await driver.wait(until.urlContains("/dashboard"), 15000);
  await driver.get(`${baseUrl}/ferramentas`);
  await assertText("Acesse os módulos liberados para o seu perfil nesta prefeitura.");
  if ((await driver.findElements(By.css(".ferr-manage-button"))).length) throw new Error("Controle administrativo exibido para funcionário");
  for (const slug of ["setores", "cargos", "controle-acesso"]) {
    if ((await driver.findElements(By.id(`ferr-card-${slug}`))).length) throw new Error(`Ferramenta administrativa exposta: ${slug}`);
  }
  await shot("tools-user-launcher");
  process.stdout.write(`OK ferramentas desktop, configuracao, acessos, mobile e launcher de funcionario\nScreenshots: ${outputDir}\n`);
} finally {
  await driver.quit();
}
