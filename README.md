# Étirements Galaxy S26 — Widget natif V3

Version conçue pour être compilée dans GitHub Actions, sans Android Studio.

Fonctions :
- widget Android natif
- chronomètre de compte à rebours via `RemoteViews`/`Chronometer`
- Lancer / Pause / Reprendre
- RAZ
- Étirement / Repos
- cycles
- réglage Étirement/Repos dans l'application
- vibration lors des changements de phase
- transitions programmées par AlarmManager, y compris en veille

Le widget utilise le Chronometer Android pour l'affichage du décompte plutôt qu'une mise à jour du widget chaque seconde.
