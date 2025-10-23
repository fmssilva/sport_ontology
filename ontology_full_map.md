classDiagram
    %% ===== ORGANIZAÇÕES E ESTRUTURAS =====
    class Organization {
        <<abstract>>
        +name: string
        +foundedYear: integer
        +country: string
    }
    
    class Federation {
        +level: FederationLevel
        +jurisdiction: string
    }
    
    class League {
        +sport: Sport
        +tier: integer
        +hasPromotion: boolean
        +hasRelegation: boolean
    }
    
    class Division {
        +name: string
        +level: integer
        +minTeams: integer
        +maxTeams: integer
    }
    
    class Competition {
        <<abstract>>
        +startDate: date
        +endDate: date
    }
    
    class Season {
        +year: integer
        +status: SeasonStatus
    }
    
    class Tournament {
        +format: TournamentFormat
        +prize: float
    }
    
    %% ===== TIMES E ESTRUTURAS =====
    class Team {
        +name: string
        +city: string
        +foundedYear: integer
        +stadiumCapacity: integer
    }
    
    class TeamCategory {
        <<abstract>>
        +minAge: integer
        +maxAge: integer
    }
    
    class SeniorTeam
    class YouthTeam
    class ReserveTeam
    class U21Team
    class U18Team
    
    %% ===== PESSOAS E PAPÉIS =====
    class Person {
        +name: string
        +birthDate: date
        +nationality: string
        +height: float
        +weight: float
    }
    
    class Role {
        <<abstract>>
        +startDate: date
        +endDate: date
        +status: RoleStatus
    }
    
    class Player {
        +position: Position
        +jerseyNumber: integer
        +marketValue: float
    }
    
    class Goalkeeper
    class Defender
    class Midfielder
    class Forward
    
    class Coach {
        +licenseLevel: string
        +specialization: string
    }
    
    class HeadCoach
    class AssistantCoach
    class GoalkeeperCoach
    
    class MedicalStaff {
        +specialization: string
    }
    
    class Physiotherapist
    class Doctor
    
    class AdministrativeStaff {
        +department: string
    }
    
    class Manager
    class Scout
    class Analyst
    
    %% ===== CONTRATOS E TRANSFERÊNCIAS =====
    class Contract {
        +startDate: date
        +endDate: date
        +salary: float
        +contractType: ContractType
    }
    
    class PermanentContract
    class LoanContract
    class TemporaryContract
    
    class Transfer {
        +transferDate: date
        +transferFee: float
        +transferType: TransferType
    }
    
    %% ===== REGRAS E REGULAMENTOS =====
    class Rule {
        +description: string
        +ruleType: RuleType
        +validFrom: date
        +validTo: date
    }
    
    class EligibilityRule {
        +minAge: integer
        +maxAge: integer
        +maxForeigners: integer
    }
    
    class FinancialRule {
        +maxSalaryBudget: float
        +minYouthPlayers: integer
    }
    
    class TechnicalRule {
        +minSquadSize: integer
        +maxSquadSize: integer
    }
    
    %% ===== RELACIONAMENTOS: ORGANIZAÇÕES =====
    Organization <|-- Federation
    Organization <|-- League
    Organization <|-- Team
    
    Federation "1" --> "*" League : governs
    League "1" --> "*" Division : contains
    Division "1" --> "*" Team : participates
    
    Competition <|-- Season
    Competition <|-- Tournament
    
    League "1" --> "*" Season : organizes
    Season "*" --> "*" Team : includes
    Division "1" --> "*" Competition : hosts
    
    %% ===== RELACIONAMENTOS: TIMES =====
    Team "1" --> "*" TeamCategory : has
    TeamCategory <|-- SeniorTeam
    TeamCategory <|-- YouthTeam
    TeamCategory <|-- ReserveTeam
    YouthTeam <|-- U21Team
    YouthTeam <|-- U18Team
    
    %% ===== RELACIONAMENTOS: PESSOAS E PAPÉIS =====
    Person "1" --> "*" Role : hasRole
    
    Role <|-- Player
    Role <|-- Coach
    Role <|-- MedicalStaff
    Role <|-- AdministrativeStaff
    
    Player <|-- Goalkeeper
    Player <|-- Defender
    Player <|-- Midfielder
    Player <|-- Forward
    
    Coach <|-- HeadCoach
    Coach <|-- AssistantCoach
    Coach <|-- GoalkeeperCoach
    
    MedicalStaff <|-- Physiotherapist
    MedicalStaff <|-- Doctor
    
    AdministrativeStaff <|-- Manager
    AdministrativeStaff <|-- Scout
    AdministrativeStaff <|-- Analyst
    
    Role "*" --> "1" Team : worksFor
    
    %% ===== RELACIONAMENTOS: CONTRATOS =====
    Contract <|-- PermanentContract
    Contract <|-- LoanContract
    Contract <|-- TemporaryContract
    
    Person "1" --> "*" Contract : signs
    Team "1" --> "*" Contract : offers
    Contract "1" --> "1" Role : governs
    
    Transfer "*" --> "1" Person : involves
    Transfer "*" --> "1" Team : fromTeam
    Transfer "*" --> "1" Team : toTeam
    
    %% ===== RELACIONAMENTOS: REGRAS =====
    Rule <|-- EligibilityRule
    Rule <|-- FinancialRule
    Rule <|-- TechnicalRule
    
    Rule "*" --> "*" Competition : appliesTo
    Rule "*" --> "*" League : regulatedBy
    Team "*" --> "*" Rule : mustComplyWith