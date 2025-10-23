import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateH2Database {
    public static void main(String[] args) {
        String url = "jdbc:h2:./sports-db";
        String user = "sa";
        String password = "";
        
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Conectado à base de dados!");
            
            Statement stmt = conn.createStatement();
            
            // Apagar tabelas se existirem (ordem inversa por causa das FK)
            System.out.println("A limpar tabelas antigas...");
            stmt.execute("DROP TABLE IF EXISTS contract");
            stmt.execute("DROP TABLE IF EXISTS player_role");
            stmt.execute("DROP TABLE IF EXISTS coach_role");
            stmt.execute("DROP TABLE IF EXISTS person");
            stmt.execute("DROP TABLE IF EXISTS team");
            
            // ========== CRIAR TABELAS ==========
            System.out.println("A criar tabelas...");
            
            // Tabela: Team
            stmt.execute("CREATE TABLE team (" +
                "team_id INTEGER PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "city VARCHAR(100), " +
                "founded_year INTEGER, " +
                "stadium_capacity INTEGER, " +
                "team_type VARCHAR(50))");
            
            // Tabela: Person
            stmt.execute("CREATE TABLE person (" +
                "person_id INTEGER PRIMARY KEY, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "birth_date DATE, " +
                "nationality VARCHAR(50), " +
                "height FLOAT, " +
                "weight FLOAT)");
            
            // Tabela: Player Role
            stmt.execute("CREATE TABLE player_role (" +
                "role_id INTEGER PRIMARY KEY, " +
                "person_id INTEGER NOT NULL, " +
                "team_id INTEGER NOT NULL, " +
                "position VARCHAR(50), " +
                "jersey_number INTEGER, " +
                "market_value FLOAT, " +
                "start_date DATE, " +
                "end_date DATE, " +
                "FOREIGN KEY (person_id) REFERENCES person(person_id), " +
                "FOREIGN KEY (team_id) REFERENCES team(team_id))");
            
            // Tabela: Coach Role
            stmt.execute("CREATE TABLE coach_role (" +
                "role_id INTEGER PRIMARY KEY, " +
                "person_id INTEGER NOT NULL, " +
                "team_id INTEGER NOT NULL, " +
                "role_type VARCHAR(50), " +
                "license_level VARCHAR(50), " +
                "start_date DATE, " +
                "end_date DATE, " +
                "FOREIGN KEY (person_id) REFERENCES person(person_id), " +
                "FOREIGN KEY (team_id) REFERENCES team(team_id))");
            
            // Tabela: Contract
            stmt.execute("CREATE TABLE contract (" +
                "contract_id INTEGER PRIMARY KEY, " +
                "person_id INTEGER NOT NULL, " +
                "team_id INTEGER NOT NULL, " +
                "contract_type VARCHAR(50), " +
                "start_date DATE, " +
                "end_date DATE, " +
                "salary FLOAT, " +
                "is_active BOOLEAN, " +
                "FOREIGN KEY (person_id) REFERENCES person(person_id), " +
                "FOREIGN KEY (team_id) REFERENCES team(team_id))");
            
            System.out.println("Tabelas criadas com sucesso!");
            System.out.println("\nA inserir dados...");
            
            // ========== INSERIR DADOS: TEAMS ==========
            System.out.println("- Inserindo teams...");
            
            // Senior Teams
            stmt.execute("INSERT INTO team VALUES (1, 'Manchester City', 'Manchester', 1880, 55000, 'SeniorTeam')");
            stmt.execute("INSERT INTO team VALUES (2, 'Real Madrid', 'Madrid', 1902, 81000, 'SeniorTeam')");
            stmt.execute("INSERT INTO team VALUES (3, 'Bayern Munich', 'Munich', 1900, 75000, 'SeniorTeam')");
            stmt.execute("INSERT INTO team VALUES (4, 'Paris Saint-Germain', 'Paris', 1970, 48000, 'SeniorTeam')");
            stmt.execute("INSERT INTO team VALUES (5, 'Barcelona', 'Barcelona', 1899, 99000, 'SeniorTeam')");
            
            // Youth Teams
            stmt.execute("INSERT INTO team VALUES (6, 'Manchester City U21', 'Manchester', 1880, 7000, 'YouthTeam')");
            stmt.execute("INSERT INTO team VALUES (7, 'Real Madrid Castilla', 'Madrid', 1902, 6000, 'YouthTeam')");
            
            // ========== INSERIR DADOS: PERSONS ==========
            System.out.println("- Inserindo pessoas...");
            
            // Players
            stmt.execute("INSERT INTO person VALUES (1, 'Erling Haaland', '2000-07-21', 'Norway', 1.95, 88.0)");
            stmt.execute("INSERT INTO person VALUES (2, 'Kevin De Bruyne', '1991-06-28', 'Belgium', 1.81, 70.0)");
            stmt.execute("INSERT INTO person VALUES (3, 'Vinicius Junior', '2000-07-12', 'Brazil', 1.76, 73.0)");
            stmt.execute("INSERT INTO person VALUES (4, 'Jude Bellingham', '2003-06-29', 'England', 1.86, 75.0)");
            stmt.execute("INSERT INTO person VALUES (5, 'Kylian Mbappe', '1998-12-20', 'France', 1.78, 73.0)");
            stmt.execute("INSERT INTO person VALUES (6, 'Harry Kane', '1993-07-28', 'England', 1.88, 86.0)");
            stmt.execute("INSERT INTO person VALUES (7, 'Ederson Moraes', '1993-08-17', 'Brazil', 1.88, 86.0)");
            stmt.execute("INSERT INTO person VALUES (8, 'Thibaut Courtois', '1992-05-11', 'Belgium', 1.99, 96.0)");
            stmt.execute("INSERT INTO person VALUES (9, 'Robert Lewandowski', '1988-08-21', 'Poland', 1.85, 81.0)");
            stmt.execute("INSERT INTO person VALUES (10, 'Pedri Gonzalez', '2002-11-25', 'Spain', 1.74, 60.0)");
            
            // Youth Players
            stmt.execute("INSERT INTO person VALUES (11, 'Rico Lewis', '2004-11-21', 'England', 1.69, 65.0)");
            stmt.execute("INSERT INTO person VALUES (12, 'Nico Paz', '2004-09-14', 'Argentina', 1.81, 73.0)");
            
            // Coaches
            stmt.execute("INSERT INTO person VALUES (20, 'Pep Guardiola', '1971-01-18', 'Spain', 1.80, 76.0)");
            stmt.execute("INSERT INTO person VALUES (21, 'Carlo Ancelotti', '1959-06-10', 'Italy', 1.79, 78.0)");
            stmt.execute("INSERT INTO person VALUES (22, 'Thomas Tuchel', '1973-08-29', 'Germany', 1.93, 85.0)");
            stmt.execute("INSERT INTO person VALUES (23, 'Luis Enrique', '1970-05-08', 'Spain', 1.82, 75.0)");
            stmt.execute("INSERT INTO person VALUES (24, 'Xavi Hernandez', '1980-01-25', 'Spain', 1.70, 68.0)");
            
            // Assistant Coaches
            stmt.execute("INSERT INTO person VALUES (25, 'Juanma Lillo', '1965-11-03', 'Spain', 1.75, 70.0)");
            stmt.execute("INSERT INTO person VALUES (26, 'Davide Ancelotti', '1989-07-22', 'Italy', 1.78, 73.0)");
            
            // ========== INSERIR DADOS: PLAYER ROLES ==========
            System.out.println("- Inserindo player roles...");
            
            // Manchester City Players
            stmt.execute("INSERT INTO player_role VALUES (1, 1, 1, 'Forward', 9, 180000000, '2022-07-01', NULL)");
            stmt.execute("INSERT INTO player_role VALUES (2, 2, 1, 'Midfielder', 17, 85000000, '2015-08-30', NULL)");
            stmt.execute("INSERT INTO player_role VALUES (3, 7, 1, 'Goalkeeper', 31, 40000000, '2017-07-01', NULL)");
            
            // Real Madrid Players
            stmt.execute("INSERT INTO player_role VALUES (4, 3, 2, 'Forward', 7, 150000000, '2018-07-01', NULL)");
            stmt.execute("INSERT INTO player_role VALUES (5, 4, 2, 'Midfielder', 5, 180000000, '2023-07-01', NULL)");
            stmt.execute("INSERT INTO player_role VALUES (6, 8, 2, 'Goalkeeper', 1, 60000000, '2018-08-09', NULL)");
            
            // Bayern Munich Players
            stmt.execute("INSERT INTO player_role VALUES (7, 6, 3, 'Forward', 9, 100000000, '2023-08-12', NULL)");
            
            // PSG Players
            stmt.execute("INSERT INTO player_role VALUES (8, 5, 4, 'Forward', 7, 180000000, '2017-08-31', NULL)");
            
            // Barcelona Players
            stmt.execute("INSERT INTO player_role VALUES (9, 9, 5, 'Forward', 9, 45000000, '2022-07-01', NULL)");
            stmt.execute("INSERT INTO player_role VALUES (10, 10, 5, 'Midfielder', 8, 80000000, '2020-09-02', NULL)");
            
            // Youth Players
            stmt.execute("INSERT INTO player_role VALUES (11, 11, 6, 'Defender', 82, 15000000, '2022-07-01', NULL)");
            stmt.execute("INSERT INTO player_role VALUES (12, 12, 7, 'Midfielder', 27, 8000000, '2023-07-01', NULL)");
            
            // Historical role (player who moved)
            stmt.execute("INSERT INTO player_role VALUES (13, 5, 2, 'Forward', 10, 160000000, '2024-07-01', NULL)");
            
            // ========== INSERIR DADOS: COACH ROLES ==========
            System.out.println("- Inserindo coach roles...");
            
            // Head Coaches
            stmt.execute("INSERT INTO coach_role VALUES (101, 20, 1, 'HeadCoach', 'UEFA Pro', '2016-07-01', NULL)");
            stmt.execute("INSERT INTO coach_role VALUES (102, 21, 2, 'HeadCoach', 'UEFA Pro', '2021-06-01', NULL)");
            stmt.execute("INSERT INTO coach_role VALUES (103, 22, 3, 'HeadCoach', 'UEFA Pro', '2024-07-01', NULL)");
            stmt.execute("INSERT INTO coach_role VALUES (104, 23, 4, 'HeadCoach', 'UEFA Pro', '2023-07-05', NULL)");
            stmt.execute("INSERT INTO coach_role VALUES (105, 24, 5, 'HeadCoach', 'UEFA Pro', '2021-11-08', NULL)");
            
            // Assistant Coaches
            stmt.execute("INSERT INTO coach_role VALUES (106, 25, 1, 'AssistantCoach', 'UEFA Pro', '2016-07-01', NULL)");
            stmt.execute("INSERT INTO coach_role VALUES (107, 26, 2, 'AssistantCoach', 'UEFA A', '2021-06-01', NULL)");
            
            // Historical coaching role
            stmt.execute("INSERT INTO coach_role VALUES (108, 20, 5, 'HeadCoach', 'UEFA Pro', '2008-06-01', '2012-06-30')");
            
            // ========== INSERIR DADOS: CONTRACTS ==========
            System.out.println("- Inserindo contratos...");
            
            // Active contracts - Players
            stmt.execute("INSERT INTO contract VALUES (1, 1, 1, 'PermanentContract', '2022-07-01', '2027-06-30', 20000000, TRUE)");
            stmt.execute("INSERT INTO contract VALUES (2, 2, 1, 'PermanentContract', '2021-04-01', '2025-06-30', 18000000, TRUE)");
            stmt.execute("INSERT INTO contract VALUES (3, 3, 2, 'PermanentContract', '2018-07-01', '2027-06-30', 15000000, TRUE)");
            stmt.execute("INSERT INTO contract VALUES (4, 4, 2, 'PermanentContract', '2023-07-01', '2029-06-30', 16000000, TRUE)");
            stmt.execute("INSERT INTO contract VALUES (5, 5, 4, 'PermanentContract', '2022-05-01', '2025-06-30', 25000000, TRUE)");
            stmt.execute("INSERT INTO contract VALUES (6, 6, 3, 'PermanentContract', '2023-08-12', '2027-06-30', 18000000, TRUE)");
            
            // Loan contract
            stmt.execute("INSERT INTO contract VALUES (7, 11, 1, 'LoanContract', '2024-01-01', '2024-06-30', 1000000, FALSE)");
            
            // Active contracts - Coaches
            stmt.execute("INSERT INTO contract VALUES (10, 20, 1, 'PermanentContract', '2023-07-01', '2025-06-30', 22000000, TRUE)");
            stmt.execute("INSERT INTO contract VALUES (11, 21, 2, 'PermanentContract', '2024-01-01', '2026-06-30', 12000000, TRUE)");
            stmt.execute("INSERT INTO contract VALUES (12, 22, 3, 'PermanentContract', '2024-07-01', '2026-06-30', 10000000, TRUE)");
            
            System.out.println("Dados inseridos com sucesso!");
            
            // ========== ESTATÍSTICAS ==========
            System.out.println("\n=== RESUMO DA BASE DE DADOS ===");
            System.out.println("- 7 teams (5 senior, 2 youth)");
            System.out.println("- 17 pessoas (12 players, 5 coaches)");
            System.out.println("- 13 player roles");
            System.out.println("- 8 coach roles (incluindo histórico)");
            System.out.println("- 13 contratos");
            System.out.println("\n✅ Base de dados 'sports-db' pronta para usar com Ontop!");
            System.out.println("📍 Localização: sports-db.mv.db");
            System.out.println("\n🔗 Connection URL para Protégé:");
            System.out.println("   jdbc:h2:CAMINHO_COMPLETO/sports-db");
            
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}