-- data.sql REVISADO

-- 1. REMOVIDO O BLOCO 'CREATE TYPE'. 
-- Deixe o Hibernate gerenciar os tipos ou trate isso como VARCHAR por enquanto para evitar o erro de sintaxe.

-- 2. Inserção do USER (Garante que o User existe primeiro)
INSERT INTO users (
    id, 
    login_username, 
    password_hash, 
    role, 
    full_name, 
    email, 
    is_active, 
    created_at, 
    updated_at
) 
VALUES (
    '00000000-0000-0000-0000-000000000001', 
    'admin', 
    -- Hash para "password"
    '$2a$10$emIaBD8GOEzd3dcXvqENwuVgwwVg9hlLY3SKMQgtofdzADy5j7mr2',
    'ADMIN', 
    'Administrador do Sistema', 
    'admin@taskmanager.com', 
    TRUE,
    NOW(), 
    NOW()
)
ON CONFLICT (login_username) DO NOTHING;

-- 3. Inserção do EMPLOYEE (Garante a vinculação)
INSERT INTO employees (
    id, 
    user_id, 
    registration_number, 
    department, 
    status, 
    hire_date, 
    is_active,
    created_at, 
    updated_at
)
VALUES (
    '00000000-0000-0000-0000-00000000000A', 
    '00000000-0000-0000-0000-000000000001', -- FK para o User acima
    'ADM-SYS', 
    'ADMINISTRATIVO', -- Certifique-se que este valor existe no enum Department.kt
    'ACTIVE',         -- Certifique-se que este valor existe no enum EmployeeStatus.kt
    CURRENT_DATE,     -- Usa data pura, não timestamp
    TRUE,
    NOW(), 
    NOW()
)
ON CONFLICT (registration_number) DO NOTHING;