Person
├── Player
│   ├── Goalkeeper
│   ├── Defender
│   └── Forward
└── Coach
    ├── HeadCoach
    └── AssistantCoach

Team
├── SeniorTeam
└── YouthTeam

Contract
├── PermanentContract
└── LoanContract
```

**Properties:**
- `playsFor` (Player → Team)
- `coaches` (Coach → Team)
- `hasPlayer` (Team → Player)
- `hasCoach` (Team → Coach)
- `hasContract` (Person → Contract)
- `hasAge`, `hasNationality`, `hasMarketValue`

**Construtores DL avançados:**
1. **Cardinality**: `TopCoach ≡ Coach ⊓ (≥ 3 coaches.Team)`
2. **Qualified Cardinality**: `ExperiencedPlayer ≡ Player ⊓ (≥ 5 playsFor.Team)`
3. **Separate Properties**: `playsFor` ↔ `hasPlayer` (bidirectional, not inverse)
4. **Functional**: `hasContract` (cada person tem 1 contrato ativo)

**Axiomas GCI:**
```
Player ⊑ Person
Player ⊑ ∃playsFor.Team
Goalkeeper ⊓ Defender ⊑ ⊥ (disjoint)
TopPlayer ≡ Player ⊓ (marketValue ≥ 50000000)