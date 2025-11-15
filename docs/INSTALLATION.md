# INSTALLATION.md
## 📦 Guide d'Installation - Tailwind Smart Plugin

### Prérequis
- **IntelliJ IDEA** 2024.1 ou version ultérieure
- **WebStorm**, **PyCharm**, ou tout autre IDE JetBrains
- **Java** 17 ou version ultérieure
- **Projet Tailwind CSS** configuré

### Installation via JetBrains Marketplace

#### Méthode 1 : Via l'IDE
1. Ouvrir IntelliJ IDEA
2. Aller dans **File > Settings** (Windows/Linux) ou **IntelliJ IDEA > Preferences** (macOS)
3. Naviguer vers **Plugins**
4. Cliquer sur **Marketplace**
5. Rechercher **"Tailwind Smart Plugin"**
6. Cliquer sur **Install**
7. Redémarrer l'IDE

#### Méthode 2 : Via le site web
1. Aller sur [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/tailwind-smart-plugin)
2. Cliquer sur **Install to IDE**
3. Sélectionner votre IDE
4. Confirmer l'installation

### Installation Manuelle

#### Depuis un fichier ZIP
1. Télécharger le fichier ZIP depuis [GitHub Releases](https://github.com/dilika/tailwind-smart-plugin/releases)
2. Ouvrir **File > Settings > Plugins**
3. Cliquer sur l'icône **⚙️** puis **Install Plugin from Disk**
4. Sélectionner le fichier ZIP téléchargé
5. Redémarrer l'IDE

#### Depuis le code source
```bash
# Cloner le repository
git clone https://github.com/dilika/tailwind-smart-plugin.git
cd tailwind-smart-plugin

# Compiler le plugin
./gradlew buildPlugin

# Installer localement
./gradlew runIde
```

### Configuration Initiale

#### 1. Détection Automatique
Le plugin détecte automatiquement votre configuration Tailwind CSS :
- Recherche `tailwind.config.js` dans la racine du projet
- Analyse les classes personnalisées
- Configure les suggestions automatiquement

#### 2. Configuration Manuelle
Si la détection automatique échoue :

1. Aller dans **File > Settings > Tailwind Smart Plugin**
2. Spécifier le chemin vers `tailwind.config.js`
3. Configurer les préférences :
   - **Suggestions contextuelles** : Activé par défaut
   - **Preview visuel** : Activé par défaut
   - **Audit intelligent** : Activé par défaut
   - **Intégration Figma** : Désactivé par défaut

#### 3. Vérification de l'Installation
Pour vérifier que le plugin fonctionne :

1. Ouvrir un fichier HTML/JSX/TSX
2. Taper `class="` dans un élément
3. Vérifier que les suggestions Tailwind apparaissent
4. Tester le raccourci `Shift+F7` pour trier les classes

### Dépannage

#### Problèmes Courants

**Q: Le plugin ne s'installe pas**
A: Vérifiez que vous utilisez une version compatible d'IntelliJ IDEA (2024.1+)

**Q: Les suggestions ne s'affichent pas**
A: Vérifiez que `tailwind.config.js` est présent dans votre projet

**Q: Le plugin ralentit l'IDE**
A: Désactivez temporairement certaines fonctionnalités dans les paramètres

**Q: Erreur de compilation**
A: Vérifiez que Java 17+ est installé et configuré

#### Logs et Debug
Pour activer les logs de debug :

1. Aller dans **Help > Diagnostic Tools > Debug Log Settings**
2. Ajouter `#com.github.dilika.tailwindsmartplugin`
3. Redémarrer l'IDE
4. Consulter les logs dans **Help > Show Log in Explorer**

### Mise à Jour

#### Mise à Jour Automatique
Le plugin se met à jour automatiquement via JetBrains Marketplace.

#### Mise à Jour Manuelle
1. Aller dans **File > Settings > Plugins**
2. Rechercher **Tailwind Smart Plugin**
3. Cliquer sur **Update** si disponible
4. Redémarrer l'IDE

### Désinstallation

#### Méthode 1 : Via l'IDE
1. Aller dans **File > Settings > Plugins**
2. Rechercher **Tailwind Smart Plugin**
3. Cliquer sur **Uninstall**
4. Redémarrer l'IDE

#### Méthode 2 : Suppression Manuelle
1. Fermer IntelliJ IDEA
2. Supprimer le dossier du plugin :
   - **Windows** : `%APPDATA%\JetBrains\IntelliJIdea2024.1\plugins\tailwind-smart-plugin`
   - **macOS** : `~/Library/Application Support/JetBrains/IntelliJIdea2024.1/plugins/tailwind-smart-plugin`
   - **Linux** : `~/.config/JetBrains/IntelliJIdea2024.1/plugins/tailwind-smart-plugin`
3. Redémarrer l'IDE

### Support

#### Ressources
- **Documentation** : [GitHub Wiki](https://github.com/dilika/tailwind-smart-plugin/wiki)
- **Issues** : [GitHub Issues](https://github.com/dilika/tailwind-smart-plugin/issues)
- **Discussions** : [GitHub Discussions](https://github.com/dilika/tailwind-smart-plugin/discussions)

#### Contact
- **Email** : support@tailwindsmartplugin.com
- **Twitter** : [@tailwindsmart](https://twitter.com/tailwindsmart)
- **Discord** : [Serveur Discord](https://discord.gg/tailwindsmart)

### Changelog
Voir [CHANGELOG.md](CHANGELOG.md) pour l'historique des versions.

### Licence
Ce plugin est distribué sous licence MIT. Voir [LICENSE](LICENSE) pour plus de détails.





