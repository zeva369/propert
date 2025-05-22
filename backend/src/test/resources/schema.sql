CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    user_id VARCHAR(255) NOT NULL,
    CONSTRAINT fk_project_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE task (
    id UUID PRIMARY KEY,
    label VARCHAR(255),
    description VARCHAR(255),
    length DOUBLE,
    project_id BIGINT NOT NULL,
    is_dummy BOOLEAN,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE TABLE task_predecessors (
    id UUID NOT NULL,
    predecessor_id UUID NOT NULL,
    PRIMARY KEY (id, predecessor_id),
    CONSTRAINT fk_task_pred_task FOREIGN KEY (id) REFERENCES task(id),
    CONSTRAINT fk_task_pred_predecessor FOREIGN KEY (predecessor_id) REFERENCES task(id)
);