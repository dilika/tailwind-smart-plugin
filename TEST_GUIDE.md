# 🧪 Guide de Test - Plugin Tailwind Smart v1.2.2

## 📦 Installation

1. **Ouvrir IntelliJ IDEA**
2. Aller dans `Settings/Preferences` > `Plugins`
3. Cliquer sur l'icône ⚙️ > `Install Plugin from Disk...`
4. Sélectionner : `build/distributions/tailwind-smart-plugin-1.2.2.zip`
5. **Redémarrer IntelliJ IDEA**

---

## ✅ Tests à Effectuer

### 1. 🔍 **Validation en Temps Réel (NOUVEAU)**

#### Test 1.1 : Détection de classes invalides
1. Créer ou ouvrir un fichier HTML/JSX/TSX/Vue
2. Ajouter un élément avec une classe Tailwind invalide :
   ```html
   <div class="bg-invalid-500 text-wrong-class">Test</div>
   ```
3. **Vérifier** : Vous devriez voir des warnings jaunes sous les classes invalides
4. **Vérifier** : Le message d'erreur devrait mentionner "does not exist in Tailwind CSS"

#### Test 1.2 : Suggestions intelligentes
1. Taper une classe avec une faute de frappe :
   ```html
   <div class="bg-blu-500">Test</div>
   ```
2. **Vérifier** : Le warning devrait suggérer `bg-blue-500` ou des classes similaires
3. **Vérifier** : Le message devrait contenir "Did you mean: bg-blue-500?"

#### Test 1.3 : Quick Fix automatique
1. Placer le curseur sur une classe invalide avec suggestions
2. Appuyer sur `Alt+Enter` (ou `Option+Enter` sur Mac)
3. **Vérifier** : Un menu contextuel apparaît avec "Replace with 'bg-blue-500'"
4. Sélectionner la suggestion
5. **Vérifier** : La classe invalide est remplacée automatiquement

#### Test 1.4 : Validation des variants
1. Tester des variants invalides :
   ```html
   <div class="invalid-variant:bg-blue-500">Test</div>
   ```
2. **Vérifier** : Warning pour variant invalide
3. Tester des variants valides :
   ```html
   <div class="hover:bg-blue-500 focus:bg-blue-600 md:bg-blue-700">Test</div>
   ```
4. **Vérifier** : Pas de warnings pour les variants valides

#### Test 1.5 : Validation des valeurs arbitraires
1. Tester une valeur arbitraire valide :
   ```html
   <div class="w-[100px] h-[200px]">Test</div>
   ```
2. **Vérifier** : Pas de warnings
3. Tester une valeur arbitraire invalide :
   ```html
   <div class="invalid-[value]">Test</div>
   ```
4. **Vérifier** : Warning pour syntaxe invalide

---

### 2. 🎨 **Fonctionnalités Existantes**

#### Test 2.1 : Auto-complétion
1. Taper `class="` dans un élément HTML
2. Commencer à taper `bg-`
3. **Vérifier** : Les suggestions Tailwind apparaissent
4. **Vérifier** : Les icônes de couleur sont affichées

#### Test 2.2 : Documentation au survol
1. Placer le curseur sur une classe Tailwind (ex: `bg-blue-500`)
2. Appuyer sur `Ctrl+Q` (ou `Cmd+J` sur Mac)
3. **Vérifier** : La documentation apparaît

#### Test 2.3 : Tri des classes
1. Sélectionner des classes Tailwind dans le désordre :
   ```html
   <div class="text-white bg-blue-500 p-4 rounded">Test</div>
   ```
2. Appuyer sur `Shift+F7`
3. **Vérifier** : Les classes sont triées dans un ordre logique

#### Test 2.4 : Folding des classes
1. Placer le curseur sur un attribut `class` avec beaucoup de classes
2. Appuyer sur `Shift+F8`
3. **Vérifier** : Les classes se replient/affichent

---

### 3. 🐛 **Tests de Régression**

#### Test 3.1 : Classes valides ne doivent pas générer de warnings
1. Utiliser des classes Tailwind standard :
   ```html
   <div class="bg-blue-500 text-white p-4 rounded-lg hover:bg-blue-600">
     Test
   </div>
   ```
2. **Vérifier** : Aucun warning ne devrait apparaître

#### Test 3.2 : Support multi-frameworks
1. Tester dans un fichier React (JSX) :
   ```jsx
   <div className="bg-blue-500">Test</div>
   ```
2. **Vérifier** : La validation fonctionne avec `className`
3. Tester dans un fichier Vue :
   ```vue
   <template>
     <div class="bg-blue-500">Test</div>
   </template>
   ```
4. **Vérifier** : La validation fonctionne dans Vue

---

## 📊 Checklist de Test

### Fonctionnalités Critiques
- [ ] Validation détecte les classes invalides
- [ ] Suggestions intelligentes fonctionnent
- [ ] Quick fixes remplacent les classes
- [ ] Variants sont validés correctement
- [ ] Valeurs arbitraires sont validées
- [ ] Pas de faux positifs (classes valides)

### Fonctionnalités Existantes
- [ ] Auto-complétion fonctionne
- [ ] Documentation au survol fonctionne
- [ ] Tri des classes fonctionne
- [ ] Folding fonctionne

### Support Multi-langages
- [ ] HTML fonctionne
- [ ] JSX/TSX fonctionne
- [ ] Vue fonctionne
- [ ] JavaScript/TypeScript fonctionne

---

## 🐛 Problèmes à Signaler

Si vous rencontrez des problèmes, notez :
1. **Type de fichier** (HTML, JSX, Vue, etc.)
2. **Classes testées**
3. **Comportement attendu vs comportement réel**
4. **Messages d'erreur** (si présents)
5. **Version d'IntelliJ** utilisée

---

## ✅ Critères de Succès

Le build est réussi si :
- ✅ Le plugin s'installe sans erreur
- ✅ La validation détecte au moins 80% des classes invalides
- ✅ Les suggestions sont pertinentes (distance de Levenshtein ≤ 3)
- ✅ Les quick fixes fonctionnent
- ✅ Aucune régression sur les fonctionnalités existantes

---

*Bon test ! 🚀*

