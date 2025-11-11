# 🧪 Guide de Test Utilisateur Complet - Phase 1

## 📋 Table des Matières

1. [Préparation](#préparation)
2. [Tests de Validation](#tests-de-validation)
3. [Tests de Preview](#tests-de-preview)
4. [Tests de Documentation](#tests-de-documentation)
5. [Tests d'Intelligence Contextuelle](#tests-dintelligence-contextuelle)
6. [Tests de Refactoring](#tests-de-refactoring)
7. [Tests de Performance](#tests-de-performance)
8. [Checklist Complète](#checklist-complète)

---

## 🔧 Préparation

### Installation du Plugin

1. **Ouvrir IntelliJ IDEA** (version 2025.2 ou supérieure)
2. Aller dans `Settings/Preferences` > `Plugins`
3. Cliquer sur l'icône ⚙️ > `Install Plugin from Disk...`
4. Sélectionner : `build/distributions/tailwind-smart-plugin-1.2.2.zip`
5. **Redémarrer IntelliJ IDEA**

### Préparation du Projet de Test

1. Créer un nouveau projet ou ouvrir un projet existant avec Tailwind CSS
2. S'assurer qu'un fichier `tailwind.config.js` existe (ou créer un projet de test)
3. Créer un fichier HTML de test : `test.html`

---

## ✅ Tests de Validation

### Test 1.1 : Validation de Classes Invalides

**Objectif** : Vérifier que les classes invalides sont détectées

**Étapes** :
1. Ouvrir `test.html`
2. Ajouter le code suivant :
   ```html
   <div class="bg-invalid-500 text-wrong-class p-4">Test</div>
   ```
3. Attendre 2-3 secondes

**Résultat Attendu** :
- ✅ Warnings jaunes sous `bg-invalid-500` et `text-wrong-class`
- ✅ Message d'erreur : "Tailwind: Class 'bg-invalid-500' does not exist in Tailwind CSS"
- ✅ Suggestions affichées : "Did you mean: bg-blue-500?"

**Critères de Succès** :
- [ ] Les warnings apparaissent automatiquement
- [ ] Les messages d'erreur sont clairs
- [ ] Les suggestions sont pertinentes

---

### Test 1.2 : Quick Fix Automatique

**Objectif** : Vérifier que les quick fixes fonctionnent

**Étapes** :
1. Dans le fichier de test, placer le curseur sur `bg-invalid-500`
2. Appuyer sur `Alt+Enter` (ou `Option+Enter` sur Mac)
3. Sélectionner "Replace with 'bg-blue-500'" (ou une suggestion similaire)

**Résultat Attendu** :
- ✅ Menu contextuel avec les suggestions
- ✅ La classe invalide est remplacée automatiquement
- ✅ Le warning disparaît après remplacement

**Critères de Succès** :
- [ ] Le quick fix apparaît dans le menu
- [ ] Le remplacement fonctionne correctement
- [ ] Le warning disparaît après correction

---

### Test 1.3 : Validation des Variants

**Objectif** : Vérifier la validation des variants

**Étapes** :
1. Tester un variant invalide :
   ```html
   <div class="invalid-variant:bg-blue-500">Test</div>
   ```
2. Tester des variants valides :
   ```html
   <div class="hover:bg-blue-500 focus:bg-blue-600 md:bg-blue-700">Test</div>
   ```
3. Tester des variants multiples :
   ```html
   <div class="hover:focus:bg-blue-500 md:hover:bg-blue-600">Test</div>
   ```

**Résultat Attendu** :
- ✅ Warning pour variant invalide
- ✅ Pas de warnings pour variants valides
- ✅ Pas de warnings pour variants multiples valides

**Critères de Succès** :
- [ ] Les variants invalides sont détectés
- [ ] Les variants valides ne génèrent pas de warnings
- [ ] Les variants multiples sont supportés

---

### Test 1.4 : Validation des Valeurs Arbitraires

**Objectif** : Vérifier la validation des valeurs arbitraires

**Étapes** :
1. Tester une valeur arbitraire valide :
   ```html
   <div class="w-[100px] h-[200px] bg-[#ff0000]">Test</div>
   ```
2. Tester une valeur arbitraire invalide :
   ```html
   <div class="invalid-[value]">Test</div>
   ```

**Résultat Attendu** :
- ✅ Pas de warnings pour valeurs arbitraires valides
- ✅ Warning pour syntaxe invalide

**Critères de Succès** :
- [ ] Les valeurs arbitraires valides sont acceptées
- [ ] Les syntaxes invalides sont détectées

---

### Test 1.5 : Détection des Conflits de Classes

**Objectif** : Vérifier la détection des conflits

**Étapes** :
1. Tester un conflit de padding :
   ```html
   <div class="p-4 px-2 py-3">Test</div>
   ```
2. Tester un conflit de margin :
   ```html
   <div class="m-4 mx-2 my-3">Test</div>
   ```
3. Tester un conflit de display :
   ```html
   <div class="flex block grid">Test</div>
   ```
4. Tester un conflit de position :
   ```html
   <div class="static fixed absolute">Test</div>
   ```

**Résultat Attendu** :
- ✅ Warnings pour chaque type de conflit
- ✅ Message : "General padding class conflicts with specific padding classes"
- ✅ Quick fix disponible pour résoudre le conflit

**Critères de Succès** :
- [ ] Les conflits de padding sont détectés
- [ ] Les conflits de margin sont détectés
- [ ] Les conflits de display sont détectés
- [ ] Les conflits de position sont détectés
- [ ] Les quick fixes fonctionnent

---

### Test 1.6 : Validation de l'Ordre des Variants

**Objectif** : Vérifier la validation de l'ordre des variants

**Étapes** :
1. Tester un ordre incorrect :
   ```html
   <div class="hover:md:bg-blue-500">Test</div>
   ```
2. Tester un ordre correct :
   ```html
   <div class="md:hover:bg-blue-500">Test</div>
   ```

**Résultat Attendu** :
- ✅ Warning pour ordre incorrect : "Responsive variants should come before state variants"
- ✅ Pas de warning pour ordre correct

**Critères de Succès** :
- [ ] L'ordre incorrect est détecté
- [ ] L'ordre correct est accepté

---

## 🎨 Tests de Preview

### Test 2.1 : Tool Window Preview

**Objectif** : Vérifier que la tool window de preview fonctionne

**Étapes** :
1. Ouvrir la tool window : `View` > `Tool Windows` > `Tailwind Preview`
2. Placer le curseur sur un élément avec des classes Tailwind :
   ```html
   <div class="bg-blue-500 text-white p-4 rounded-lg">Test</div>
   ```
3. Observer la tool window

**Résultat Attendu** :
- ✅ Tool window s'ouvre à droite
- ✅ Preview visuel s'affiche
- ✅ Classes affichées : "Classes: bg-blue-500, text-white, p-4, rounded-lg"
- ✅ Description affichée

**Critères de Succès** :
- [ ] La tool window s'ouvre correctement
- [ ] Le preview s'affiche
- [ ] Les informations sont correctes

---

### Test 2.2 : Auto-update du Preview

**Objectif** : Vérifier que le preview se met à jour automatiquement

**Étapes** :
1. Ouvrir la tool window de preview
2. Cocher "Auto-update on cursor move"
3. Déplacer le curseur entre différents éléments avec classes Tailwind
4. Observer le preview

**Résultat Attendu** :
- ✅ Le preview se met à jour automatiquement
- ✅ L'image change selon les classes
- ✅ Les informations se mettent à jour

**Critères de Succès** :
- [ ] L'auto-update fonctionne
- [ ] Le preview change en temps réel
- [ ] Pas de lag notable

---

### Test 2.3 : Preview avec Dark Mode

**Objectif** : Vérifier le toggle dark mode

**Étapes** :
1. Dans la tool window, cocher "Dark mode"
2. Observer le preview
3. Décocher "Dark mode"
4. Observer à nouveau

**Résultat Attendu** :
- ✅ Le preview change avec dark mode activé
- ✅ Les classes dark: sont appliquées
- ✅ Le preview revient à la normale quand désactivé

**Critères de Succès** :
- [ ] Le toggle fonctionne
- [ ] Le preview change visuellement
- [ ] Les classes dark: sont visibles

---

### Test 2.4 : Preview Responsive

**Objectif** : Vérifier le sélecteur de breakpoint

**Étapes** :
1. Dans la tool window, sélectionner différents breakpoints (sm, md, lg, xl, 2xl)
2. Observer le preview pour chaque breakpoint
3. Tester avec des classes qui ont des variants responsive :
   ```html
   <div class="bg-blue-500 md:bg-green-500 lg:bg-red-500">Test</div>
   ```

**Résultat Attendu** :
- ✅ Le preview change selon le breakpoint sélectionné
- ✅ Les variants responsive sont appliqués
- ✅ Le preview reflète les changements

**Critères de Succès** :
- [ ] Le sélecteur fonctionne
- [ ] Les breakpoints sont appliqués
- [ ] Le preview change correctement

---

## 📚 Tests de Documentation

### Test 3.1 : Documentation au Survol

**Objectif** : Vérifier la documentation enrichie

**Étapes** :
1. Placer le curseur sur une classe Tailwind (ex: `bg-blue-500`)
2. Appuyer sur `Ctrl+Q` (ou `Cmd+J` sur Mac)
3. Observer la documentation

**Résultat Attendu** :
- ✅ Documentation HTML riche s'affiche
- ✅ Badge de version (Tailwind v3/v4)
- ✅ Badge de catégorie (Background, Typography, etc.)
- ✅ Section "CSS Equivalent"
- ✅ Section "Example" avec code
- ✅ Section "Official Documentation" avec lien
- ✅ Section "Quick Reference" avec cheat sheet

**Critères de Succès** :
- [ ] La documentation est riche et complète
- [ ] Les liens vers la doc officielle fonctionnent
- [ ] Le cheat sheet est utile
- [ ] Les exemples sont pertinents

---

### Test 3.2 : Lien vers Documentation Officielle

**Objectif** : Vérifier les liens vers docs.tailwindcss.com

**Étapes** :
1. Ouvrir la documentation pour différentes classes :
   - `bg-blue-500` → devrait pointer vers `/background-color`
   - `text-lg` → devrait pointer vers `/font-size`
   - `p-4` → devrait pointer vers `/padding`
2. Cliquer sur le lien "View on Tailwind CSS Docs →"

**Résultat Attendu** :
- ✅ Les liens sont corrects
- ✅ Les liens s'ouvrent dans le navigateur
- ✅ Les pages correspondent aux classes

**Critères de Succès** :
- [ ] Les liens sont fonctionnels
- [ ] Les URLs sont correctes
- [ ] Les pages correspondent

---

### Test 3.3 : Cheat Sheet

**Objectif** : Vérifier le cheat sheet par catégorie

**Étapes** :
1. Ouvrir la documentation pour différentes catégories :
   - `bg-blue-500` → devrait montrer des classes Background
   - `text-lg` → devrait montrer des classes Typography
   - `p-4` → devrait montrer des classes Spacing
2. Observer la section "Quick Reference"

**Résultat Attendu** :
- ✅ Cheat sheet affiché par catégorie
- ✅ Classes liées affichées
- ✅ Classes communes de la catégorie affichées

**Critères de Succès** :
- [ ] Le cheat sheet est pertinent
- [ ] Les classes sont bien catégorisées
- [ ] Les suggestions sont utiles

---

## 🧠 Tests d'Intelligence Contextuelle

### Test 4.1 : Détection de Framework

**Objectif** : Vérifier la détection automatique du framework

**Étapes** :
1. Créer un projet React (avec `package.json` contenant `"react"`)
2. Ouvrir un fichier JSX
3. Vérifier que le framework est détecté (via logs ou comportement)

**Résultat Attendu** :
- ✅ Framework React détecté
- ✅ Suggestions adaptées au framework

**Critères de Succès** :
- [ ] La détection fonctionne pour React
- [ ] La détection fonctionne pour Vue
- [ ] La détection fonctionne pour Next.js/Nuxt

---

### Test 4.2 : Suggestions Basées sur l'Historique

**Objectif** : Vérifier que les suggestions utilisent l'historique

**Étapes** :
1. Utiliser plusieurs fois la classe `bg-blue-500`
2. Taper `bg-` dans un nouvel élément
3. Observer les suggestions

**Résultat Attendu** :
- ✅ `bg-blue-500` apparaît en haut des suggestions
- ✅ Les classes fréquemment utilisées sont priorisées

**Critères de Succès** :
- [ ] L'historique influence les suggestions
- [ ] Les classes fréquentes sont priorisées
- [ ] L'historique s'accumule correctement

---

### Test 4.3 : Détection de Design System

**Objectif** : Vérifier la détection du design system

**Étapes** :
1. Créer un projet avec shadcn/ui (dossier `components/ui`)
2. Vérifier la détection (via logs ou comportement)

**Résultat Attendu** :
- ✅ Design system détecté (shadcn/ui, Tailwind UI, etc.)
- ✅ Patterns spécifiques suggérés

**Critères de Succès** :
- [ ] La détection fonctionne pour shadcn/ui
- [ ] La détection fonctionne pour Tailwind UI
- [ ] Les patterns sont adaptés

---

### Test 4.4 : Analyse de Composants

**Objectif** : Vérifier l'analyse contextuelle des composants

**Étapes** :
1. Créer un élément `<button>` avec `class="`
2. Observer les suggestions

**Résultat Attendu** :
- ✅ Suggestions adaptées aux boutons
- ✅ Patterns de boutons suggérés (px-4 py-2 bg-blue-500, etc.)

**Critères de Succès** :
- [ ] Les suggestions sont contextuelles
- [ ] Les patterns sont pertinents
- [ ] La confiance est calculée correctement

---

## 🔧 Tests de Refactoring

### Test 5.1 : Consolidation de Classes

**Objectif** : Vérifier la consolidation automatique

**Étapes** :
1. Créer un élément avec classes redondantes :
   ```html
   <div class="p-4 px-2 py-3 m-4 mx-2">Test</div>
   ```
2. Utiliser le service de refactoring (via action ou API)

**Résultat Attendu** :
- ✅ Classes consolidées : `px-2 py-3 mx-2` (p-4 et m-4 supprimés)
- ✅ Pas de perte de fonctionnalité

**Critères de Succès** :
- [ ] La consolidation fonctionne
- [ ] Les classes redondantes sont supprimées
- [ ] Le résultat est correct

---

### Test 5.2 : Extraction de Patterns

**Objectif** : Vérifier l'extraction de patterns

**Étapes** :
1. Créer plusieurs boutons avec le même pattern :
   ```html
   <button class="px-4 py-2 bg-blue-500 text-white rounded">Button 1</button>
   <button class="px-4 py-2 bg-blue-500 text-white rounded">Button 2</button>
   ```
2. Utiliser le service de refactoring

**Résultat Attendu** :
- ✅ Pattern "Button" détecté
- ✅ Suggestion de composant/extraction

**Critères de Succès** :
- [ ] Les patterns sont détectés
- [ ] Les suggestions sont pertinentes

---

## ⚡ Tests de Performance

### Test 6.1 : Cache des Validations

**Objectif** : Vérifier que le cache améliore les performances

**Étapes** :
1. Valider une classe invalide plusieurs fois : `bg-invalid-500`
2. Observer le temps de réponse
3. Vérifier les statistiques du cache (si disponible)

**Résultat Attendu** :
- ✅ Première validation : temps normal
- ✅ Validations suivantes : temps réduit (cache)
- ✅ Pas de dégradation de performance

**Critères de Succès** :
- [ ] Le cache fonctionne
- [ ] Les performances sont améliorées
- [ ] Pas de fuites mémoire

---

### Test 6.2 : Lazy Loading

**Objectif** : Vérifier le lazy loading des classes

**Étapes** :
1. Taper `bg-` dans la complétion
2. Observer le temps de chargement
3. Taper `text-` ensuite

**Résultat Attendu** :
- ✅ Chargement rapide des suggestions
- ✅ Pas de blocage de l'interface
- ✅ Classes chargées à la demande

**Critères de Succès** :
- [ ] Le lazy loading fonctionne
- [ ] L'interface reste réactive
- [ ] Les suggestions sont rapides

---

## 📊 Checklist Complète

### Validation
- [ ] Classes invalides détectées
- [ ] Quick fixes fonctionnent
- [ ] Variants validés correctement
- [ ] Valeurs arbitraires validées
- [ ] Conflits détectés
- [ ] Ordre des variants validé

### Preview
- [ ] Tool window s'ouvre
- [ ] Preview s'affiche correctement
- [ ] Auto-update fonctionne
- [ ] Dark mode fonctionne
- [ ] Breakpoints fonctionnent

### Documentation
- [ ] Documentation riche affichée
- [ ] Liens vers doc officielle fonctionnent
- [ ] Cheat sheet affiché
- [ ] Exemples pertinents

### Intelligence
- [ ] Framework détecté
- [ ] Historique utilisé
- [ ] Design system détecté
- [ ] Analyse contextuelle fonctionne

### Refactoring
- [ ] Consolidation fonctionne
- [ ] Patterns détectés

### Performance
- [ ] Cache fonctionne
- [ ] Lazy loading fonctionne
- [ ] Pas de lag

---

## 🐛 Rapport de Bugs

Si vous rencontrez des problèmes, notez :

1. **Type de problème** : Bug, Performance, UX
2. **Fichier testé** : HTML, JSX, Vue, etc.
3. **Classes testées** : Liste des classes
4. **Comportement attendu** : Ce qui devrait se passer
5. **Comportement réel** : Ce qui se passe réellement
6. **Messages d'erreur** : Si présents
7. **Version d'IntelliJ** : Version utilisée
8. **Version du plugin** : 1.2.2

---

## ✅ Critères de Succès Globaux

Le plugin est considéré comme réussi si :

- ✅ **80%+ des validations** fonctionnent correctement
- ✅ **90%+ des suggestions** sont pertinentes
- ✅ **Aucun crash** lors des tests
- ✅ **Performance acceptable** (< 500ms pour la plupart des opérations)
- ✅ **UX fluide** sans lag notable

---

## 📝 Notes de Test

**Date du test** : _______________

**Testeur** : _______________

**Version IntelliJ** : _______________

**Version du plugin** : 1.2.2

**Résultat global** : ☐ Réussi  ☐ Échec  ☐ Partiel

**Commentaires** :
_________________________________________________
_________________________________________________
_________________________________________________

---

*Bon test ! 🚀*

