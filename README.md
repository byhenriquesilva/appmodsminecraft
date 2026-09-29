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


