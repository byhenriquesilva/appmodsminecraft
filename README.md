# Atlas de Mods — app Android

Cliente nativo (Kotlin + Jetpack Compose) do site hospedado na Vercel — sem
WebView. Consome as rotas `/api/*` descritas em `docs/API.md` do repositório
do site; a chave do GitHub nunca sai do servidor, o app só guarda a senha de
admin, criptografada no aparelho.

## Antes de compilar

1. Aplique o patch `api-android-patch.zip` (rotas `/api/*`) no repositório do
   site, se ainda não aplicou, e faça o deploy na Vercel.
2. Abra este projeto no Android Studio (Ladybird/Koala ou mais novo).
3. Em `app/build.gradle.kts`, troque `DEFAULT_BASE_URL` pela URL do seu
   deploy — ou deixe como está e configure pela tela de Ajustes do próprio
   app depois de instalado (fica salvo).
4. Sync do Gradle e Run. `minSdk 26` (Android 8+), `compileSdk`/`targetSdk 35`.

Este projeto não foi compilado neste ambiente (não há SDK do Android aqui) —
os arquivos foram revisados um a um, mas o primeiro `Gradle Sync` no Android
Studio é o teste real. Erros de sync mais prováveis: versão do AGP/Kotlin
desatualizada no seu Android Studio (ajuste em `libs.versions.toml`).

## Estrutura

- `network/` — `AtlasApi` (Retrofit), `ApiClient`, `model/Mod.kt` (todos os
  DTOs, espelhando `docs/API.md`).
- `data/` — `SecretStore` (URL + senha, via `EncryptedSharedPreferences`) e
  `ModsRepository` (traduz erros HTTP na mensagem em português que o
  servidor devolveu).
- `ui/screens/` — `catalog` (lista + busca), `detail` (ficha do mod,
  download, galeria, descrição), `enviar` (um mod ou vários, escolha de
  versão), `gerenciar` (seleção múltipla + exclusão), `settings` (URL e
  senha).
- `ui/nav/AtlasNavHost.kt` — as cinco telas acima.

## Limitações conhecidas do esqueleto

- **Descrição do mod:** o servidor manda HTML sanitizado; o app usa
  `Html.fromHtml` (texto, links, listas), que ignora `<table>` e não baixa
  `<img>` inline. Está documentado em `ui/components/HtmlText.kt`, com a
  alternativa (WebView isolada só pra esse HTML) comentada ali.
- **Sem testes automatizados, sem Hilt, sem paginação** — é um esqueleto
  funcional pensado pra você evoluir, não um produto terminado.
- **Ícone do app:** usa o `ic_launcher` padrão do Android Studio (o projeto
  não inclui um `mipmap` customizado) — troque pelo assistente de ícones do
  próprio Android Studio (botão direito em `res` → New → Image Asset).
