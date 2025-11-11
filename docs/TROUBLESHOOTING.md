# TROUBLESHOOTING.md
## 🔧 Dépannage - Tailwind Smart Plugin

### Problèmes Courants

#### 🚫 Les suggestions ne s'affichent pas

**Symptômes :**
- Aucune suggestion Tailwind CSS dans l'autocomplétion
- Le plugin semble installé mais ne fonctionne pas

**Solutions :**
1. **Vérifier la configuration Tailwind**
   ```bash
   # Vérifier que tailwind.config.js existe
   ls -la tailwind.config.js
   
   # Vérifier le contenu
   cat tailwind.config.js
   ```

2. **Redémarrer l'IDE**
   - Fermer complètement IntelliJ IDEA
   - Rouvrir le projet
   - Attendre que l'indexation se termine

3. **Vérifier les paramètres du plugin**
   - Aller dans **File > Settings > Tailwind Smart Plugin**
   - Vérifier que "Suggestions contextuelles" est activé
   - Spécifier manuellement le chemin vers `tailwind.config.js`

4. **Vérifier les types de fichiers supportés**
   - HTML : `.html`, `.htm`
   - JSX : `.jsx`, `.tsx`
   - Vue : `.vue`
   - PHP : `.php`
   - Svelte : `.svelte`
   - Astro : `.astro`

#### 🐌 Le plugin ralentit l'IDE

**Symptômes :**
- L'IDE devient lent lors de la saisie
- Délais dans l'autocomplétion
- Utilisation mémoire élevée

**Solutions :**
1. **Désactiver temporairement certaines fonctionnalités**
   - Aller dans **File > Settings > Tailwind Smart Plugin**
   - Désactiver "Preview visuel" si non nécessaire
   - Désactiver "Audit intelligent" pour les gros projets

2. **Optimiser la configuration**
   ```javascript
   // tailwind.config.js
   module.exports = {
     content: [
       "./src/**/*.{html,js,jsx,ts,tsx,vue,php,svelte,astro}",
       // Limiter le scope si nécessaire
     ],
     // ...
   }
   ```

3. **Augmenter la mémoire allouée à l'IDE**
   - Aller dans **Help > Edit Custom VM Options**
   - Ajouter : `-Xmx4g` (ou plus selon votre RAM)

#### 🔍 L'audit ne détecte rien

**Symptômes :**
- Aucun problème détecté par l'audit
- Messages d'erreur dans les logs

**Solutions :**
1. **Vérifier la configuration du projet**
   - S'assurer que le projet est correctement ouvert
   - Vérifier que les fichiers sont dans le scope du projet

2. **Vérifier les permissions**
   - S'assurer que l'IDE a accès en lecture aux fichiers
   - Vérifier les permissions sur `tailwind.config.js`

3. **Activer les logs de debug**
   - Aller dans **Help > Diagnostic Tools > Debug Log Settings**
   - Ajouter : `#com.github.dilika.tailwindsmartplugin`
   - Redémarrer et consulter les logs

#### 🎨 Le preview visuel ne fonctionne pas

**Symptômes :**
- Aucun preview affiché
- Erreurs dans la console

**Solutions :**
1. **Vérifier l'installation de Tailwind CSS**
   ```bash
   # Vérifier que Tailwind CSS est installé
   npm list tailwindcss
   
   # Ou avec yarn
   yarn list tailwindcss
   ```

2. **Vérifier la configuration Tailwind**
   ```javascript
   // tailwind.config.js
   module.exports = {
     content: ["./src/**/*.{html,js,jsx,ts,tsx}"],
     theme: {
       extend: {},
     },
     plugins: [],
   }
   ```

3. **Vérifier les classes CSS**
   - S'assurer que les classes Tailwind sont valides
   - Vérifier qu'elles sont dans le scope de `content`

#### 🔗 L'intégration Figma ne fonctionne pas

**Symptômes :**
- Impossible de se connecter à Figma
- Erreurs d'authentification

**Solutions :**
1. **Vérifier le token d'accès Figma**
   - Aller sur [Figma > Settings > Personal Access Tokens](https://www.figma.com/settings)
   - Créer un nouveau token
   - Copier le token dans les paramètres du plugin

2. **Vérifier les permissions du token**
   - Le token doit avoir les permissions de lecture
   - Vérifier que le fichier Figma est accessible

3. **Vérifier l'URL du fichier Figma**
   - L'URL doit être au format : `https://www.figma.com/file/[FILE_ID]/[FILE_NAME]`
   - Vérifier que le fichier n'est pas privé

#### 📱 Les raccourcis clavier ne fonctionnent pas

**Symptômes :**
- Les raccourcis ne déclenchent pas les actions
- Conflits avec d'autres raccourcis

**Solutions :**
1. **Vérifier les raccourcis configurés**
   - Aller dans **File > Settings > Keymap**
   - Rechercher "Tailwind"
   - Vérifier que les raccourcis sont assignés

2. **Résoudre les conflits**
   - Identifier les raccourcis en conflit
   - Réassigner les raccourcis si nécessaire

3. **Raccourcis par défaut**
   - `Shift+F7` : Trier les classes sélectionnées
   - `Shift+Alt+F7` : Trier toutes les classes du fichier
   - `Shift+F8` : Basculer le pliage des classes
   - `Shift+Alt+F8` : Plier toutes les classes
   - `Shift+Ctrl+F8` : Déplier toutes les classes

### Problèmes de Performance

#### 🐌 Suggestions lentes

**Solutions :**
1. **Réduire le nombre de classes**
   ```javascript
   // tailwind.config.js
   module.exports = {
     content: [
       "./src/components/**/*.{html,js,jsx,ts,tsx}",
       // Limiter au strict nécessaire
     ],
   }
   ```

2. **Désactiver les suggestions contextuelles**
   - Aller dans **File > Settings > Tailwind Smart Plugin**
   - Désactiver "Suggestions contextuelles"

3. **Utiliser le cache**
   - Le plugin met en cache les suggestions
   - Redémarrer l'IDE pour vider le cache si nécessaire

#### 💾 Utilisation mémoire élevée

**Solutions :**
1. **Limiter le scope des fichiers**
   ```javascript
   // tailwind.config.js
   module.exports = {
     content: [
       "./src/**/*.{html,js,jsx,ts,tsx}",
       // Éviter les patterns trop larges comme "./**/*"
     ],
   }
   ```

2. **Désactiver les fonctionnalités lourdes**
   - Désactiver "Audit intelligent" pour les gros projets
   - Désactiver "Preview visuel" si non nécessaire

3. **Augmenter la mémoire de l'IDE**
   - Aller dans **Help > Edit Custom VM Options**
   - Ajouter : `-Xmx4g` ou plus

### Problèmes de Configuration

#### ⚙️ Configuration Tailwind non détectée

**Solutions :**
1. **Vérifier la structure du projet**
   ```
   project-root/
   ├── tailwind.config.js  ← Doit être à la racine
   ├── src/
   │   └── components/
   └── package.json
   ```

2. **Vérifier le contenu de tailwind.config.js**
   ```javascript
   module.exports = {
     content: ["./src/**/*.{html,js,jsx,ts,tsx}"],
     theme: {
       extend: {},
     },
     plugins: [],
   }
   ```

3. **Configurer manuellement**
   - Aller dans **File > Settings > Tailwind Smart Plugin**
   - Spécifier le chemin vers `tailwind.config.js`

#### 🎯 Classes personnalisées non reconnues

**Solutions :**
1. **Vérifier la configuration des classes personnalisées**
   ```javascript
   // tailwind.config.js
   module.exports = {
     theme: {
       extend: {
         colors: {
           'custom-blue': '#1E90FF',
         },
         spacing: {
           'custom-sm': '8px',
         },
       },
     },
   }
   ```

2. **Redémarrer l'IDE**
   - Fermer complètement l'IDE
   - Rouvrir le projet
   - Attendre la re-analyse

3. **Vérifier le cache**
   - Supprimer le cache du plugin
   - Redémarrer l'IDE

### Logs et Debug

#### 📋 Activer les logs de debug

1. **Via l'IDE**
   - Aller dans **Help > Diagnostic Tools > Debug Log Settings**
   - Ajouter : `#com.github.dilika.tailwindsmartplugin`
   - Redémarrer l'IDE

2. **Via les paramètres**
   - Aller dans **File > Settings > Tailwind Smart Plugin**
   - Activer "Mode debug"

#### 📁 Localisation des logs

**Windows :**
```
%APPDATA%\JetBrains\IntelliJIdea2024.1\log\idea.log
```

**macOS :**
```
~/Library/Logs/JetBrains/IntelliJIdea2024.1/idea.log
```

**Linux :**
```
~/.config/JetBrains/IntelliJIdea2024.1/log/idea.log
```

#### 🔍 Analyser les logs

Rechercher les messages suivants :
- `[TailwindContextAnalyzer]` : Logs de l'analyseur de contexte
- `[TailwindVisualPreviewService]` : Logs du service de preview
- `[TailwindCodeAuditor]` : Logs de l'auditeur
- `ERROR` : Messages d'erreur
- `WARN` : Messages d'avertissement

### Support et Contact

#### 📞 Obtenir de l'aide

1. **GitHub Issues**
   - [Créer une issue](https://github.com/dilika/tailwind-smart-plugin/issues)
   - Inclure les logs et la configuration

2. **Discord**
   - [Rejoindre le serveur Discord](https://discord.gg/tailwindsmart)
   - Chat en temps réel avec la communauté

3. **Email**
   - support@tailwindsmartplugin.com
   - Réponse sous 24-48h

#### 📝 Signaler un bug

Inclure les informations suivantes :
- Version d'IntelliJ IDEA
- Version du plugin
- Système d'exploitation
- Logs d'erreur
- Étapes pour reproduire le problème
- Configuration du projet

#### 💡 Suggérer une fonctionnalité

1. **GitHub Discussions**
   - [Créer une discussion](https://github.com/dilika/tailwind-smart-plugin/discussions)
   - Décrire la fonctionnalité souhaitée

2. **Discord**
   - Canal #feature-requests
   - Discussion avec la communauté

### Ressources Utiles

#### 📚 Documentation
- [Guide Utilisateur](USER_GUIDE.md)
- [Guide Développeur](DEVELOPER_GUIDE.md)
- [API Reference](API_REFERENCE.md)

#### 🔗 Liens Externes
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/tailwind-smart-plugin)

#### 🆘 FAQ
- [FAQ GitHub](https://github.com/dilika/tailwind-smart-plugin/wiki/FAQ)
- [Troubleshooting GitHub](https://github.com/dilika/tailwind-smart-plugin/wiki/Troubleshooting)




