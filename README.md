# ip-guard for Gerrit 3.5.12

This repository builds the `ip-guard.jar` plugin for Gerrit **3.5.12** with Java 11 via GitHub Actions.

## How to get the JAR (no local build)
1. Create a new GitHub repository (empty) and upload the contents of this folder.
2. Go to **Actions** tab → run **build-ip-guard** (or push once to trigger).
3. Open the workflow run → **Artifacts** → download `ip-guard-3.5.12` → contains `ip-guard.jar`.

## Deploy
```
cp ip-guard.jar <GERRIT_SITE>/plugins/
/home/source/sraid5/gerrit/Gitreview/bin/gerrit.sh restart
```
