# 🧪 Guide de Test - Freemium Features

## 📋 Vue d'Ensemble

Ce guide vous aide à tester toutes les fonctionnalités free vs premium du plugin Tailwind CSS Smart Plugin.

---

## 🔧 Préparation

### 1. Installation du Plugin
```bash
# Build le plugin
./gradlew buildPlugin

# Installer dans l'IDE de test
./gradlew runIde
```

### 2. Configuration de Test

#### Tester en Mode Free
- Par défaut, le plugin démarre en mode free
- Vérifier que `TailwindLicenseService.isPremiumActive()` retourne `false`

#### Tester en Mode Premium/Trial
- Option 1 : Utiliser l'UI dans Settings > Tools > Tailwind CSS Smart Plugin > "Start 7-Day Free Trial"
- Option 2 : Pour les tests de développement, utiliser la propriété système :
  ```bash
  -Dtailwind.premium=true
  ```

---

## 🆓 Tests Free Version

### 1. ✅ Completion Basique

**Test** :
1. Ouvrir un fichier HTML/JSX/TSX
2. Taper `class="`
3. Commencer à taper une classe Tailwind (ex: `bg-`)

**Résultats Attendus** :
- ✅ Suggestions de classes apparaissent
- ✅ Icônes grises (pas de couleurs)
- ✅ Pas de priorité intelligente (ordre alphabétique basique)
- ✅ Pas de preview dans le dropdown
- ✅ Pas d'apprentissage de l'historique

**Fichiers à Vérifier** :
- `completion/TailwindCompletionContributor.kt`
- `icons/TailwindPremiumIconRegistry.kt`

---

### 2. ✅ Validation Basique

**Test** :
1. Écrire une classe invalide : `class="bg-invalid-class"`
2. Utiliser Alt+Enter sur la classe invalide

**Résultats Attendus** :
- ✅ Classe invalide surlignée en rouge
- ✅ 1 seule suggestion de correction disponible
- ✅ Quick fix fonctionne

**Fichiers à Vérifier** :
- `inspection/TailwindInvalidClassInspection.kt`

---

### 3. ✅ Preview Basique

**Test** :
1. Ouvrir `View > Tool Windows > Tailwind Preview`
2. Placer le curseur sur un attribut `class="bg-blue-500 p-4 rounded"`
3. Observer le preview

**Résultats Attendus** :
- ✅ Preview s'affiche
- ❌ Dark mode checkbox désactivé
- ❌ Breakpoint selector désactivé
- ✅ Preview basique fonctionne

**Fichiers à Vérifier** :
- `preview/TailwindPreviewToolWindow.kt`

---

### 4. ✅ Documentation Basique

**Test** :
1. Hover sur une classe Tailwind (ex: `bg-blue-500`)
2. Observer la documentation

**Résultats Attendus** :
- ✅ Documentation textuelle s'affiche
- ❌ Pas d'exemples interactifs
- ❌ Pas de CSS équivalents détaillés
- ✅ Liens vers la documentation officielle

**Fichiers à Vérifier** :
- `documentation/TailwindDocumentationProvider.kt`

---

### 5. ✅ Outils Basiques

**Test** :
1. Sélectionner des classes : `class="p-4 m-2 bg-blue-500"`
2. Utiliser Shift+F7 pour trier
3. Utiliser Shift+F8 pour fold/unfold

**Résultats Attendus** :
- ✅ Tri fonctionne
- ✅ Folding fonctionne
- ✅ Tous les outils basiques disponibles

**Fichiers à Vérifier** :
- `actions/SortTailwindClassesAction.kt`
- `folding/TailwindClassFoldingBuilder.kt`

---

### 6. ✅ Refactoring (Non Disponible)

**Test** :
1. Essayer d'utiliser les fonctionnalités de refactoring

**Résultats Attendus** :
- ❌ Refactoring non disponible
- ✅ Dialog d'upgrade s'affiche si tenté

**Fichiers à Vérifier** :
- `refactoring/TailwindRefactoringService.kt`

---

### 7. ✅ Audit (Non Disponible)

**Test** :
1. Essayer d'utiliser les fonctionnalités d'audit

**Résultats Attendus** :
- ❌ Audit non disponible
- ✅ Dialog d'upgrade s'affiche si tenté

**Fichiers à Vérifier** :
- `audit/TailwindCodeAuditor.kt`

---

## 💎 Tests Premium Version

### 1. ✅ Icônes Premium

**Test** :
1. Activer le trial ou premium
2. Ouvrir completion sur `class="bg-blue-500"`

**Résultats Attendus** :
- ✅ Icônes colorées avec vraies couleurs
- ✅ 292 combinaisons de couleurs supportées
- ✅ Icônes spécialisées par catégorie

**Vérification** :
```kotlin
// Dans TailwindPremiumIconRegistry.kt
val icon = TailwindPremiumIconRegistry.getPremiumIcon("bg-blue-500")
// Doit retourner une icône colorée bleue
```

---

### 2. ✅ Validation Avancée

**Test** :
1. Activer premium
2. Écrire une classe invalide : `class="bg-invalid-class"`
3. Utiliser Alt+Enter

**Résultats Attendus** :
- ✅ Jusqu'à 5 suggestions de correction
- ✅ Suggestions intelligentes basées sur Levenshtein
- ✅ Quick fixes multiples fonctionnent

**Vérification** :
```kotlin
// Dans TailwindInvalidClassInspection.kt
val maxSuggestions = if (PremiumFeatureGate.isPremium()) 5 else 1
```

---

### 3. ✅ Preview Avancé

**Test** :
1. Activer premium
2. Ouvrir Preview tool window
3. Tester dark mode et breakpoints

**Résultats Attendus** :
- ✅ Dark mode checkbox activé et fonctionnel
- ✅ Breakpoint selector activé et fonctionnel
- ✅ Preview se met à jour en temps réel
- ✅ Live updates fonctionnent

**Vérification** :
```kotlin
// Dans TailwindPreviewToolWindow.kt
darkModeCheckbox.isEnabled = PremiumFeatureGate.isPremium()
breakpointCombo.isEnabled = PremiumFeatureGate.isPremium()
```

---

### 4. ✅ Completion Intelligente

**Test** :
1. Activer premium
2. Utiliser plusieurs classes dans le projet
3. Observer les suggestions lors de la completion

**Résultats Attendus** :
- ✅ Priorité intelligente (classes fréquentes en haut)
- ✅ Suggestions basées sur l'historique
- ✅ Suggestions contextuelles avancées
- ✅ Preview dans le dropdown

**Vérification** :
```kotlin
// Dans TailwindCompletionContributor.kt
if (PremiumFeatureGate.isPremium()) {
    enhanceSuggestionsWithHistory(...)
    calculatePremiumPriority(...)
}
```

---

### 5. ✅ Documentation Enrichie

**Test** :
1. Activer premium
2. Hover sur une classe Tailwind

**Résultats Attendus** :
- ✅ Exemples interactifs
- ✅ CSS équivalents détaillés
- ✅ Cheat sheet intégré
- ✅ Documentation contextuelle

**Vérification** :
```kotlin
// Dans TailwindDocumentationProvider.kt
val enhancedDoc = if (PremiumFeatureGate.isPremium()) {
    enhancedDocumentation.generateDocumentation(className)
} else null
```

---

### 6. ✅ Refactoring

**Test** :
1. Activer premium
2. Utiliser les fonctionnalités de refactoring

**Résultats Attendus** :
- ✅ Consolidation de classes fonctionne
- ✅ Extraction de composants fonctionne
- ✅ Optimisation fonctionne

**Vérification** :
```kotlin
// Dans TailwindRefactoringService.kt
if (!PremiumFeatureGate.checkPremiumAccess(...)) {
    return classes // Non disponible
}
```

---

### 7. ✅ Audit & Analytics

**Test** :
1. Activer premium
2. Utiliser les fonctionnalités d'audit

**Résultats Attendus** :
- ✅ Audit complet fonctionne
- ✅ Statistiques d'utilisation disponibles
- ✅ Rapports de qualité générés
- ✅ Suggestions d'optimisation

**Vérification** :
```kotlin
// Dans TailwindCodeAuditor.kt
if (!PremiumFeatureGate.checkPremiumAccess(...)) {
    return AuditReport.empty(...) // Non disponible
}
```

---

## 🎯 Tests UI d'Activation

### 1. ✅ Affichage du Statut

**Test** :
1. Ouvrir `Settings > Tools > Tailwind CSS Smart Plugin`
2. Observer la section "License & Premium"

**Résultats Attendus** :
- ✅ Statut affiché correctement (Free/Trial/Premium)
- ✅ Jours restants affichés si trial actif
- ✅ Boutons appropriés affichés

---

### 2. ✅ Activation du Trial

**Test** :
1. En mode free, cliquer sur "Start 7-Day Free Trial"
2. Observer les changements

**Résultats Attendus** :
- ✅ Message de confirmation s'affiche
- ✅ Statut passe à "Trial Active"
- ✅ Compteur de jours apparaît
- ✅ Bouton "Start Trial" disparaît
- ✅ Toutes les fonctionnalités premium activées

---

### 3. ✅ Upgrade vers Premium

**Test** :
1. Cliquer sur "Upgrade to Premium"
2. Observer le comportement

**Résultats Attendus** :
- ✅ Navigateur s'ouvre sur la page d'upgrade
- ✅ URL correcte : `https://plugins.jetbrains.com/plugin/PTAILWINDCSSSUP`
- ✅ Gestion d'erreur si navigateur ne peut pas s'ouvrir

---

### 4. ✅ Expiration du Trial

**Test** :
1. Simuler l'expiration du trial (modifier `trialEndDate` dans les settings)
2. Redémarrer l'IDE
3. Observer le comportement

**Résultats Attendus** :
- ✅ Statut revient à "Free Version"
- ✅ Fonctionnalités premium désactivées
- ✅ Bouton "Upgrade to Premium" réapparaît

---

## 🔍 Tests de Gating

### Vérifier que le Gating Fonctionne

**Test pour chaque fonctionnalité premium** :

1. **En mode Free** :
   - Vérifier que la fonctionnalité est limitée ou non disponible
   - Vérifier que le dialog d'upgrade s'affiche si tenté

2. **En mode Premium/Trial** :
   - Vérifier que la fonctionnalité fonctionne complètement
   - Vérifier qu'aucun dialog d'upgrade n'apparaît

---

## 📊 Checklist de Test

### Free Version
- [ ] Completion basique fonctionne
- [ ] Icônes grises affichées
- [ ] 1 quick fix seulement
- [ ] Preview basique fonctionne
- [ ] Dark mode désactivé
- [ ] Breakpoints désactivés
- [ ] Documentation basique
- [ ] Refactoring non disponible
- [ ] Audit non disponible
- [ ] Pas d'apprentissage

### Premium/Trial Version
- [ ] Icônes colorées affichées
- [ ] 5 quick fixes disponibles
- [ ] Dark mode fonctionne
- [ ] Breakpoints fonctionnent
- [ ] Completion intelligente fonctionne
- [ ] Documentation enrichie fonctionne
- [ ] Refactoring fonctionne
- [ ] Audit fonctionne
- [ ] Apprentissage fonctionne

### UI d'Activation
- [ ] Statut affiché correctement
- [ ] Trial s'active correctement
- [ ] Upgrade ouvre le navigateur
- [ ] Compteur de jours fonctionne
- [ ] Mise à jour automatique du statut

---

## 🐛 Problèmes Connus et Solutions

### Problème : Trial ne s'active pas
**Solution** : Vérifier que `TailwindLicenseSettings` est bien enregistré dans `plugin.xml`

### Problème : Fonctionnalités premium toujours disponibles en free
**Solution** : Vérifier que `PremiumFeatureGate.isPremium()` retourne `false` en free

### Problème : UI ne se met pas à jour après activation du trial
**Solution** : Appeler `updateLicenseStatus()` et `updateTrialDays()` après activation

---

## 📝 Notes de Test

### Environnement de Test Recommandé
- IntelliJ IDEA 2024.1+
- Projet avec Tailwind CSS configuré
- Fichiers HTML/JSX/TSX pour tester

### Données de Test
- Classes Tailwind valides : `bg-blue-500`, `p-4`, `rounded-lg`
- Classes invalides : `bg-invalid-class`, `invalid-class`
- Classes avec conflits : `p-4 px-2`, `m-4 mx-2`

---

*Dernière mise à jour : Novembre 2024*

