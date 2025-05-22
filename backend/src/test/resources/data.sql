-- Usuarios
INSERT INTO users (id, username, password, role) VALUES
  ('user1', 'usuario1', '$2a$10$Hj5QNRukt8X9.B13pE7wq.TVDlQcB63Ur.3MAzTvV3GpPYkVy5BFK', 'USER'),
  ('user2', 'usuario2', '$2a$10$h0LYTcCuTolxqee2fCsIwOyp/n9afQ7de3GAi4ef5GQoAzAq.OXdu', 'ADMIN');

-- Proyectos
INSERT INTO project (id, name, description, user_id) VALUES
  (1, 'Proyecto Alpha', 'Primer proyecto', 'user1'),
  (2, 'Proyecto Beta', 'Segundo proyecto', 'user2');

-- Tareas
INSERT INTO task (id, label, description, length, project_id, is_dummy) VALUES
  ('4c97a9a8-017e-48ae-969b-f4bfc26476f6', 'task1', 'Tarea inicial', 5.0, 1, FALSE),
  ('fe251a45-c590-4116-b8fc-2a4eaad756dd', 'task2', 'Tarea intermedia', 3.0, 1, FALSE),
  ('594f02b6-e566-4490-99a9-b339a7c009f5', 'task3', 'Tarea final', 2.0, 2, FALSE);

-- Predecesores
INSERT INTO task_predecessors (id, predecessor_id) VALUES
  ('594f02b6-e566-4490-99a9-b339a7c009f5', 'fe251a45-c590-4116-b8fc-2a4eaad756dd');