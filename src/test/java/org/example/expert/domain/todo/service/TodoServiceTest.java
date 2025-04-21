package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    String title;
    String contents;
    String weather;
    User user;

    @BeforeEach
    void setUp() {
        weather = "Sunny";
        title = "title";
        contents = "contents";
        user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    @DisplayName("성공 - Todo 등록 정상")
    void saveTodo_success() {
        //given
        AuthUser authUser = new AuthUser(1L, "writer@email.com", UserRole.USER);

        User writer = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(writer, "id", 1L);

        TodoSaveRequest request = new TodoSaveRequest(title, contents);
        Todo todo = new Todo(request.getTitle(), request.getContents(), weather, writer);
        ReflectionTestUtils.setField(todo, "id", 1L);

        given(weatherClient.getTodayWeather()).willReturn(weather);
        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        //when
        TodoSaveResponse result = todoService.saveTodo(authUser, request);

        //then
        assertNotNull(result);
        assertEquals(todo.getId(), result.getId());
        assertEquals(todo.getTitle(), result.getTitle());
        assertEquals(todo.getContents(), result.getContents());
        assertEquals(todo.getWeather(), result.getWeather());
        assertEquals(writer.getId(), result.getUser().getId());
        assertEquals(writer.getEmail(), result.getUser().getEmail());
    }

    @Test
    @DisplayName("성공 - getTodos")
    void getTodos_success() {
        //given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size);

        Todo todo = new Todo(title, contents, weather, user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        Page<Todo> todos = new PageImpl<>(List.of(todo), pageable, 1);

        given(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(todos);

        //when
        Page<TodoResponse> responses = todoService.getTodos(page, size);

        //then
        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());
        assertEquals(1, responses.getTotalPages());
        TodoResponse response = responses.getContent().get(0);
        assertEquals(todo.getId(), response.getId());
        assertEquals(todo.getTitle(), response.getTitle());
        assertEquals(todo.getWeather(), response.getWeather());
    }

    @Test
    @DisplayName("성공 - getTodo")
    void getTodo_success() {
        //given
        long todoId = 1L;
        Todo todo = new Todo(title, contents, weather, user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        given(todoRepository.findByIdWithUser(todoId)).willReturn(java.util.Optional.of(todo));

        //when
        TodoResponse response = todoService.getTodo(todoId);

        //then
        assertNotNull(response);
        assertEquals(todo.getId(), response.getId());
        assertEquals(todo.getTitle(), response.getTitle());
        assertEquals(todo.getWeather(), response.getWeather());
        assertEquals(todo.getUser().getId(), response.getUser().getId());
        assertEquals(todo.getUser().getEmail(), response.getUser().getEmail());
    }
}
