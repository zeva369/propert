-- Usuarios
INSERT INTO users (id, username, password, role) VALUES
  ('user1', 'usuario1', '$2a$10$Hj5QNRukt8X9.B13pE7wq.TVDlQcB63Ur.3MAzTvV3GpPYkVy5BFK', 'USER'),
  ('user2', 'usuario2', '$2a$10$h0LYTcCuTolxqee2fCsIwOyp/n9afQ7de3GAi4ef5GQoAzAq.OXdu', 'ADMIN');

-- Proyectos
INSERT INTO project (id, name, description, user_id) VALUES
  (1, 'Proyecto Alpha', 'Primer proyecto', 'user1'),
  (2, 'Proyecto Beta', 'Segundo proyecto', 'user2');

-- Tareas
INSERT INTO task (id, description, length, project_id, is_dummy) VALUES
  ('task1', 'Tarea inicial', 5.0, 1, FALSE),
  ('task2', 'Tarea intermedia', 3.0, 1, FALSE),
  ('task3', 'Tarea final', 2.0, 2, FALSE);

-- Predecesores
INSERT INTO task_predecessors (id, predecessor_id) VALUES
  ('task2', 'task1'),
  ('task3', 'task2');