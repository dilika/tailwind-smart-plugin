# ⚡ Guide de Test Rapide - Phase 1

## 🚀 Tests Essentiels (15 minutes)

### 1. Validation (3 min)

```html
<!-- Test 1: Classes invalides -->
<div class="bg-invalid-500 text-wrong">Test</div>
```
✅ **Attendu** : Warnings jaunes + suggestions

```html
<!-- Test 2: Quick fix -->
<div class="bg-blu-500">Test</div>
```
✅ **Attendu** : Alt+Enter → "Replace with 'bg-blue-500'"

```html
<!-- Test 3: Conflits -->
<div class="p-4 px-2">Test</div>
```
✅ **Attendu** : Warning de conflit + quick fix

---

### 2. Preview (3 min)

1. Ouvrir `View` > `Tool Windows` > `Tailwind Preview`
2. Placer curseur sur : `<div class="bg-blue-500 text-white p-4 rounded-lg">Test</div>`
3. ✅ **Attendu** : Preview visuel s'affiche

4. Cocher "Dark mode" → ✅ Preview change
5. Sélectionner "md" dans breakpoint → ✅ Preview change

---

### 3. Documentation (3 min)

1. Curseur sur `bg-blue-500` → `Ctrl+Q` (ou `Cmd+J`)
2. ✅ **Attendu** : Documentation riche avec :
   - CSS Equivalent
   - Example
   - Lien "View on Tailwind CSS Docs →"
   - Quick Reference (cheat sheet)

---

### 4. Intelligence (3 min)

1. Utiliser `bg-blue-500` plusieurs fois
2. Taper `bg-` → ✅ `bg-blue-500` en haut des suggestions
3. Créer `<button class="` → ✅ Suggestions de boutons

---

### 5. Performance (3 min)

1. Valider `bg-invalid-500` plusieurs fois
2. ✅ **Attendu** : Plus rapide après la première fois (cache)

---

## ✅ Checklist Rapide

- [ ] Warnings pour classes invalides
- [ ] Quick fixes fonctionnent
- [ ] Preview tool window s'ouvre
- [ ] Dark mode toggle fonctionne
- [ ] Documentation riche s'affiche
- [ ] Suggestions basées sur historique
- [ ] Pas de crash
- [ ] Performance acceptable

---

**Temps total** : ~15 minutes

**Si tous les tests passent** → ✅ Phase 1 validée !

