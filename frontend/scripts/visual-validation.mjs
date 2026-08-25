import { mkdir, writeFile } from "node:fs/promises";
import { Builder, By, until } from "selenium-webdriver";
import firefox from "selenium-webdriver/firefox.js";

const baseUrl = process.env.HACKGOV_FRONTEND_URL || "http://127.0.0.1:5173";
const outputDir = process.env.HACKGOV_SCREENSHOT_DIR || "/tmp/hackgov-visual";
const driverPath = process.env.GECKODRIVER_PATH || "/tmp/geckodriver";
const options = new firefox.Options().addArguments("-headless");
const service = new firefox.ServiceBuilder(driverPath);
const driver = await new Builder().forBrowser("firefox").setFirefoxOptions(options)
  .setFirefoxService(service).build();

async function screenshot(name) {
  await writeFile(`${outputDir}/${name}.png`, await driver.takeScreenshot(), "base64");
}

async function visit(path, expected, screenshotName) {
  await driver.get(`${baseUrl}${path}`);
  await driver.wait(async () => (await driver.findElement(By.css("body")).getText()).includes(expected), 15000);
  const body = await driver.findElement(By.css("body")).getText();
  if (/Application error|Unexpected Application Error|Erro interno do servidor/i.test(body)) {
    throw new Error(`Erro visual encontrado em ${path}`);
  }
  await screenshot(screenshotName);
  process.stdout.write(`OK ${path} -> ${expected}\n`);
}

try {
  await mkdir(outputDir, { recursive: true });
  await driver.manage().setTimeouts({ implicit: 3000, pageLoad: 20000, script: 10000 });
  await driver.manage().window().setRect({ width: 1440, height: 1000 });
  await driver.get(`${baseUrl}/login`);
  await driver.findElement(By.id("email")).sendKeys("admin@admin.com");
  await driver.findElement(By.id("password")).sendKeys("senha123");
  await driver.findElement(By.css("#loginForm button[type=submit]")).click();
  await driver.wait(until.urlContains("/dashboard"), 15000);
  await driver.wait(until.elementLocated(By.css(".dashboard")), 15000);
  await screenshot("01-dashboard-desktop");

  const routes = [
    ["/agenda", "Agenda", "02-agenda"],
    ["/caixa-entrada", "Caixa de Entrada", "03-inbox"],
    ["/documentos", "Documentos", "04-documents"],
    ["/clientes", "Clientes Gerais", "05-clients"],
    ["/patrulha-agricola", "Patrulha Agricola", "06-agriculture"],
    ["/importacao", "Importacao de Dados", "07-import-preview"],
    ["/processos", "Requisições de Processo", "08-processes"],
    ["/perfil", "Meu perfil", "09-profile-sessions"],
  ];
  for (const route of routes) {
    await visit(...route);
    if (route[0] === "/perfil") {
      let securityButton = null;
      for (const button of await driver.findElements(By.css(".perfil-acc-head"))) {
        if ((await button.getText()).includes("Segurança")) securityButton = button;
      }
      if (securityButton) {
        await securityButton.click();
        await driver.wait(until.elementLocated(By.css(".device-card")), 5000);
        await screenshot("09-profile-sessions-open");
      }
    }
  }

  await visit("/importacao", "Importacao de Dados", "10-import-before-upload");
  const csvPath = "/tmp/hackgov-visual-import.csv";
  await writeFile(csvPath, "Nome;Descricao;Ativo\nSetor Visual;Validacao visual;sim\n", "utf8");
  await driver.findElement(By.css('.import-upload input[type="file"]')).sendKeys(csvPath);
  await driver.findElement(By.css(".import-upload button")).click();
  try {
    await driver.wait(async () => (await driver.findElement(By.css("body")).getText()).toLowerCase().includes("preview"), 15000);
  } catch (error) {
    await screenshot("11-import-preview-error");
    const body = await driver.findElement(By.css("body")).getText();
    throw new Error(`Preview nao apareceu. Conteudo da pagina: ${body.slice(0, 1200)}`, { cause: error });
  }
  await screenshot("11-import-after-upload");

  await visit("/dashboard", "Mensagens", "12-chat-closed");
  const chatToggle = await driver.findElement(By.css(".chat-action-btn"));
  await chatToggle.click();
  await driver.wait(until.elementLocated(By.css(".msg-tabs")), 5000);
  await screenshot("13-chat-open");
  const firstChat = await driver.findElement(By.css(".msg-item.msg-item-button"));
  await firstChat.click();
  await driver.wait(until.elementLocated(By.css(".msg-compose")), 5000);
  await driver.findElement(By.css('.msg-compose input[type="file"]'));
  await screenshot("13-chat-conversation-attachment");

  await driver.manage().window().setRect({ width: 390, height: 844 });
  await visit("/dashboard", "Início", "14-dashboard-mobile");
  process.stdout.write(`Screenshots: ${outputDir}\n`);
} finally {
  await driver.quit();
}
