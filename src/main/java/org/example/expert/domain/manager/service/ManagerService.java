package org.example.expert.domain.manager.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;

    // 헬퍼 메서드: 일정 소유자 검증
    private void validateTodoHelper(User user, Todo todo) {
        if (todo.getUser() == null) {
            throw new InvalidRequestException("일정 소유자 정보가 없습니다.");
        }
        if (!Objects.equals(user.getId(), todo.getUser().getId())) {
            throw new InvalidRequestException("일정 소유자만 접근할 수 있습니다.");
        }
    }

    @Transactional
    public ManagerSaveResponse saveManager(AuthUser authUser, long todoId, ManagerSaveRequest managerSaveRequest) {
        // 일정을 만든 유저
        User user = User.fromAuthUser(authUser);
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new InvalidRequestException(
                        String.format("Todo(ID: %d)가 존재하지 않습니다.", todoId)
                ));

        validateTodoHelper(user, todo);

        User managerUser = userRepository.findById(managerSaveRequest.getManagerUserId())
                .orElseThrow(() -> new InvalidRequestException(
                        String.format("등록하려고 하는 담당자 유저(ID: %d)가 존재하지 않습니다.", managerSaveRequest.getManagerUserId())
                ));

        if (Objects.equals(user.getId(), managerUser.getId())) {
            throw new InvalidRequestException("일정 작성자는 본인을 담당자로 등록할 수 없습니다.");
        }

        Manager newManagerUser = new Manager(managerUser, todo);
        Manager savedManagerUser = managerRepository.save(newManagerUser);

        return new ManagerSaveResponse(
                savedManagerUser.getId(),
                new UserResponse(managerUser.getId(), managerUser.getEmail())
        );
    }

    @Transactional(readOnly = true)
    public List<ManagerResponse> getManagers(long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new InvalidRequestException(
                        String.format("Todo(ID: %d)가 존재하지 않습니다.", todoId)
                ));

        List<Manager> managerList = managerRepository.findByTodoIdWithUser(todo.getId());

        // DTO 변환 로직 간소화 (Stream API)
        return managerList.stream()
                .map(manager -> new ManagerResponse(
                        manager.getId(),
                        new UserResponse(
                                manager.getUser().getId(),
                                manager.getUser().getEmail()
                        )
                ))
                .toList();
    }

    @Transactional
    public void deleteManager(AuthUser authUser, long todoId, long managerId) {
        User user = User.fromAuthUser(authUser);
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new InvalidRequestException(
                        String.format("Todo(ID: %d)가 존재하지 않습니다.", todoId)
                ));

        validateTodoHelper(user, todo);

        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new InvalidRequestException(
                        String.format("Manager(ID: %d)가 존재하지 않습니다.", managerId)
                ));

        if (!Objects.equals(todo.getId(), manager.getTodo().getId())) {
            throw new InvalidRequestException("해당 일정에 등록된 담당자가 아닙니다.");
        }

        managerRepository.delete(manager);
    }
}
