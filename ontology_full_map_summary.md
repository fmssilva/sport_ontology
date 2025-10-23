# Full Ontology Map Summary

Organization (abstrata)
├── Federation (FIFA, UEFA, FPF)
│   └── governs → League
├── League (Premier League, La Liga, Liga Portugal)
│   └── contains → Division
│       └── participates ← Team
└── Team (Benfica, Porto, Sporting)
    └── has → TeamCategory (Senior, Youth, Reserve)
```

**Vantagens sobre BD relacional:**
- ✅ **Inferência automática**: Se Team pertence a Division, e Division pertence a League, então Team está sob jurisdição de League
- ✅ **Regras transitivas**: `underJurisdiction` é transitiva - propaga automaticamente
- ✅ **Validação**: Reasoner detecta se Team está em múltiplas Divisions do mesmo nível (inconsistência)

---

### 2️⃣ **HIERARQUIA DE PESSOAS E PAPÉIS** (Multi-role temporal)

**Estrutura:**
```
Person
└── hasRole → Role (abstrata, temporal)
    ├── Player
    │   ├── Goalkeeper (disjoint)
    │   ├── Defender (disjoint)
    │   ├── Midfielder (disjoint)
    │   └── Forward (disjoint)
    ├── Coach
    │   ├── HeadCoach
    │   ├── AssistantCoach
    │   └── GoalkeeperCoach
    ├── MedicalStaff
    │   ├── Physiotherapist
    │   └── Doctor
    └── AdministrativeStaff
        ├── Manager
        ├── Scout
        └── Analyst
```

**Casos complexos modelados:**
- 👤 **Multi-role**: Pessoa pode ser Player (2020-2025) E depois Coach (2025-2030)
- ⚠️ **Conflitos detectáveis**: Não pode ser HeadCoach de 2 teams ao mesmo tempo
- 🔄 **Transições**: Ex-jogador vira treinador → reasoner infere `hasCoachingExperience`

**Axiomas interessantes:**
```
ExperiencedCoach ≡ Coach ⊓ (∃wasPlayer.Player)
TopPlayer ≡ Player ⊓ (marketValue ≥ 50000000)
```

---

### 3️⃣ **CATEGORIAS DE TIMES** (Hierarquia interna)

**Estrutura:**
```
TeamCategory (abstrata)
├── SeniorTeam (age ≥ 18)
├── ReserveTeam (backup players)
└── YouthTeam
    ├── U21Team (age < 21)
    ├── U18Team (age < 18)
    └── U15Team (age < 15)
```

**Regras complexas:**
```
U21Player ≡ Player ⊓ (age < 21) ⊓ (playsIn some U21Team)
PromotablePlayer ≡ YouthPlayer ⊓ (age ≥ 18) ⊓ (performance > 7.5)
```

**Inferências:**
- Se jogador tem 20 anos E está em YouthTeam → pode ser promovido a SeniorTeam
- Se jogador tem 19 anos E está em U21Team → é elegível para U21 competitions

---

### 4️⃣ **CONTRATOS E TRANSFERÊNCIAS** (Temporal & Dinâmico)

**Estrutura:**
```
Contract (temporal)
├── PermanentContract (sem data fim fixa)
├── LoanContract (empréstimo, 6-24 meses)
└── TemporaryContract (< 6 meses)

Transfer
├── fromTeam → Team
├── toTeam → Team
├── transferType: {Permanent, Loan, Free}
```

**Casos modelados:**
- 🔀 **Loan chains**: Jogador emprestado do Team A → Team B → Team C
- ⚠️ **Overlapping contracts**: Reasoner detecta contratos sobrepostos (erro)
- 💰 **Transfer windows**: Regras de elegibilidade temporal

**Axiomas:**
```
ActivePlayer ≡ Player ⊓ (∃hasContract.(Contract ⊓ isActive))
LoanedPlayer ≡ Player ⊓ (∃hasContract.LoanContract)
FreeAgent ≡ Player ⊓ ¬(∃hasContract)
```

---

### 5️⃣ **REGRAS E ELEGIBILIDADE** (Multi-nível)

**Estrutura:**
```
Rule (abstrata)
├── EligibilityRule
│   - minAge, maxAge
│   - maxForeigners (ex: max 3 por jogo)
│   - nationalityRestrictions
├── FinancialRule
│   - maxSalaryBudget (Financial Fair Play)
│   - minYouthPlayers (ex: 4 da academia)
└── TechnicalRule
    - minSquadSize (ex: 18)
    - maxSquadSize (ex: 25)
```

**Aplicação hierárquica:**
```
FIFA Rule → UEFA Rule → National League Rule → Division Rule → Team
     ↓           ↓              ↓                  ↓            ↓
  Global    Continental    Country Level      Tier Level   Specific
```

**Inferências poderosas:**
```
CompliantTeam ≡ Team ⊓ (∀mustComplyWith.Rule satisfies)
ViolatingTeam ≡ Team ⊓ (∃violates.Rule)
EligiblePlayer ≡ Player ⊓ (∀eligibilityRule.satisfies)
```

---

## 🔥 Construtores DL Avançados a Usar

### 1. **Number Restrictions** (Cardinalidade)
```
FullSquad ≡ Team ⊓ (≥ 18 hasPlayer)
SmallSquad ≡ Team ⊓ (≤ 15 hasPlayer)
ProlifcCoach ≡ Coach ⊓ (≥ 3 coached.Team)
```

### 2. **Qualified Cardinality**
```
TopAcademy ≡ Team ⊓ (≥ 10 hasPlayer.YouthPlayer)
InternationalTeam ≡ Team ⊓ (≥ 5 hasPlayer.ForeignPlayer)
```

### 3. **Role Chains** (Propriedade transitiva)
```
underJurisdiction ⊑ underJurisdiction ∘ governedBy
// Se Team está em Division, e Division está em League, então Team está sob League
```

### 4. **Property Characteristics**
```
hasContract: Functional (cada role tem 1 contrato ativo)
isTeamMate: Symmetric, Transitive
supervises: Asymmetric, Irreflexive
```

### 5. **Nominals** (Individuals específicos)
```
ChampionsLeague ≡ {UCL}
TopLeagues ≡ {PremierLeague, LaLiga, SerieA, Bundesliga, Ligue1}
```

---

## 💡 Exemplos de Inferências Complexas

### Exemplo 1: Detecção de Conflitos
```
Input: 
  ronaldo hasRole player1
  player1 worksFor manchesterUnited
  player1 worksFor alNassr
  player1.startDate = 2023-01-01
  
Reasoner detecta: INCONSISTENTE
Razão: Não pode ter 2 contratos ativos simultâneos
```

### Exemplo 2: Classificação Automática
```
Input:
  guardiola hasRole coach1
  coach1 coached manchesterCity
  coach1 coached barcelona
  coach1 coached bayernMunich
  
Reasoner infere: guardiola ∈ TopCoach
Razão: (≥ 3 coached.EliteClub)
```

### Exemplo 3: Elegibilidade
```
Input:
  joaoFelix age 24
  joaoFelix nationality Portuguese
  joaoFelix marketValue 80000000
  
Reasoner infere:
  - joaoFelix ∈ SeniorPlayer (age ≥ 18)
  - joaoFelix ∈ TopPlayer (value > 50M)
  - joaoFelix ∈ EligibleForUEFA (age < 25 OR homegrownPlayer)