# USER_GUIDE.md
## 🚀 Guide Utilisateur - Tailwind Smart Plugin

### Installation
1. Ouvrir IntelliJ IDEA
2. Aller dans Settings > Plugins
3. Rechercher "Tailwind Smart Plugin"
4. Installer et redémarrer

### Fonctionnalités Principales

#### 🧠 Intelligence Contextuelle
- **Suggestions Automatiques** : Le plugin détecte automatiquement le type de composant
- **Patterns Prédéfinis** : Accès à 50+ patterns UI prêts à l'emploi
- **Apprentissage** : Le plugin apprend vos préférences

#### 🎨 Preview Visuel
- **Aperçu Temps Réel** : Voir le rendu CSS instantanément
- **Palette Interactive** : Sélecteur de couleurs intégré
- **Outils Visuels** : Espacement et typographie visuels

#### 🔧 Outils de Productivité
- **Audit Intelligent** : Détection automatique des problèmes
- **Refactoring** : Optimisation automatique des classes
- **Recherche Globale** : Recherche sémantique dans tout le projet

#### 🤝 Collaboration
- **Snippets Partagés** : Créer et partager des snippets
- **Analytics** : Rapports d'utilisation détaillés
- **Synchronisation** : Sync cloud des préférences

#### 🔗 Intégrations
- **Figma** : Import/sync bidirectionnel
- **Frameworks** : Support Next.js, Vue, React, etc.
- **Design System** : Génération automatique

### Raccourcis Clavier
- `Shift+F7` : Trier les classes sélectionnées
- `Shift+Alt+F7` : Trier toutes les classes du fichier
- `Shift+F8` : Basculer le pliage des classes
- `Shift+Alt+F8` : Plier toutes les classes
- `Shift+Ctrl+F8` : Déplier toutes les classes

### Configuration
1. Aller dans Settings > Tailwind Smart Plugin
2. Configurer les préférences
3. Activer/désactiver les fonctionnalités
4. Configurer l'intégration Figma

### Utilisation Avancée

#### Intelligence Contextuelle
Le plugin analyse automatiquement le contexte DOM pour fournir des suggestions pertinentes :

```html
<!-- Le plugin détecte automatiquement que c'est un bouton -->
<button class="|"> <!-- Suggestions : btn-primary, btn-secondary, etc. -->
    Click me
</button>

<!-- Le plugin détecte automatiquement que c'est une carte -->
<div class="card |"> <!-- Suggestions : card-default, card-shadow, etc. -->
    Content
</div>
```

#### Preview Visuel
Accédez au preview visuel en temps réel :

1. Sélectionner des classes Tailwind
2. Appuyer sur `Ctrl+Shift+P` (ou configurer un raccourci personnalisé)
3. Voir le rendu CSS instantanément

#### Audit Intelligent
Le plugin détecte automatiquement les problèmes :

- **Classes dupliquées** : `bg-blue-500 bg-blue-500`
- **Classes trop spécifiques** : `[width:100px]` au lieu de `w-25`
- **Classes inutilisées** : Classes définies mais jamais utilisées

#### Refactoring Automatique
Optimisez vos classes automatiquement :

1. Sélectionner les classes à optimiser
2. Appuyer sur `Ctrl+Shift+R`
3. Choisir l'optimisation souhaitée

#### Snippets Collaboratifs
Créez et partagez des snippets :

1. Sélectionner des classes
2. Appuyer sur `Ctrl+Shift+S`
3. Nommer et décrire le snippet
4. Partager avec l'équipe

### Dépannage

#### Problèmes Courants

**Q: Les suggestions ne s'affichent pas**
A: Vérifiez que `tailwind.config.js` est présent dans votre projet

**Q: Le preview ne fonctionne pas**
A: Assurez-vous que Tailwind CSS est installé et configuré

**Q: L'audit ne détecte rien**
A: Vérifiez que le plugin a accès aux fichiers de votre projet

#### Support
- **GitHub Issues** : [Lien vers le repository]
- **Email** : support@tailwindsmartplugin.com
- **Documentation** : [Lien vers la documentation complète]

### Changelog
Voir [CHANGELOG.md](CHANGELOG.md) pour l'historique des versions.

### Licence
Ce plugin est distribué sous licence MIT. Voir [LICENSE](LICENSE) pour plus de détails.




