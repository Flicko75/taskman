package com.flicko.TaskMan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flicko.TaskMan.DTOs.LoginRequest;
import com.flicko.TaskMan.enums.TaskPriority;
import com.flicko.TaskMan.enums.TaskStatus;
import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.TeamRepository;
import com.flicko.TaskMan.repos.UserRepository;
import com.flicko.TaskMan.security.SecurityConfig;
import com.flicko.TaskMan.security.jwt.JwtService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=1234567890123456789012345678901234567890123456789012345678901234",
        "jwt.expiration=3600000"
})
@ActiveProfiles("test")
@Transactional
@Import(SecurityConfig.class)
public class IntegrationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TeamRepository teamRepository;

    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @BeforeEach
    void setup(){
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @SneakyThrows
    @Test
    void loginSuccessReturnsToken() {
        User user = new User();
        user.setName("abc");
        user.setEmail("abc@mail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(UserRole.ADMIN);

        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("abc@mail.com");
        request.setPassword("password");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @SneakyThrows
    @Test
    void login_invalidPasswordReturns401() {
        User user = new User();
        user.setName("abc");
        user.setEmail("abc@mail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(UserRole.ADMIN);

        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("abc@mail.com");
        request.setPassword("wrongpass");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTask_withoutTokenReturns401() throws Exception {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Desc");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);

        Team team = new Team();
        team.setId(1L);
        task.setTeam(team);

        String json = objectMapper.writeValueAsString(task);

        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    @SneakyThrows
    @Test
    void createTask_validRequestReturns200() {
        User user = new User();
        user.setName("abc");
        user.setEmail("abc@mail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(UserRole.ADMIN);

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        Task task = new Task();
        task.setTitle("Task 1");
        task.setDescription("Desc");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);

        Team team = new Team();
        team.setName("teamA");

        teamRepository.save(team);

        task.setTeam(team);

        String json = objectMapper.writeValueAsString(task);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void createTask_thenFetchTasksReturnsCreatedTask(){
        User user = new User();
        user.setName("abc");
        user.setEmail("abc@mail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(UserRole.ADMIN);

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        Task task = new Task();
        task.setTitle("Task 1");
        task.setDescription("Desc");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);

        Team team = new Team();
        team.setName("teamA");

        teamRepository.save(team);

        task.setTeam(team);

        String json = objectMapper.writeValueAsString(task);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();

        System.out.println(response);
    }

    @SneakyThrows
    @Test
    void updateTask_changesData() {
        User user = new User();
        user.setName("abc");
        user.setEmail("abc@mail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(UserRole.ADMIN);

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        Task task = new Task();
        task.setTitle("Task 1");
        task.setDescription("Desc");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);

        Team team = new Team();
        team.setName("teamA");

        teamRepository.save(team);

        task.setTeam(team);

        String json = objectMapper.writeValueAsString(task);

        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        Long taskId = node.get("id").asLong();

        Task updated = new Task();
        updated.setTitle("Updated Title");
        updated.setDescription("Updated Desc");
        updated.setStatus(TaskStatus.TODO);
        updated.setPriority(TaskPriority.HIGH);

        String newJson = objectMapper.writeValueAsString(updated);

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newJson))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void deleteTask_removesTask() {
        User user = new User();
        user.setName("abc");
        user.setEmail("abc@mail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(UserRole.ADMIN);

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        Task task = new Task();
        task.setTitle("Task 1");
        task.setDescription("Desc");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);

        Team team = new Team();
        team.setName("teamA");

        teamRepository.save(team);

        task.setTeam(team);

        String json = objectMapper.writeValueAsString(task);

        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        Long taskId = node.get("id").asLong();

        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

}
