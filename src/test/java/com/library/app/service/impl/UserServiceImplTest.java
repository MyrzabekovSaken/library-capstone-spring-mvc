package com.library.app.service.impl;

import com.library.app.dao.UserDao;
import com.library.app.dto.UserDto;
import com.library.app.model.Order;
import com.library.app.model.Role;
import com.library.app.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    // Константы
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "reader";
    private static final String PASSWORD = "secret";
    private static final String ENCODED_PASSWORD = "encoded";
    private static final String ROLE_READER = "ROLE_READER";
    private static final String NEW_MAIL_COM = "new@mail.com";
    private static final String BLOCKED = "BLOCKED";
    private static final String OLD = "old";
    private static final String ACTIVE = "ACTIVE";
    private static final String OLD_PASS = "old-pass";
    private static final String SPACES = "  ";
    private static final String USER_NOT_FOUND = "User not found";

    // Моки
    @Mock
    private UserDao userDao;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    // Инжект мокс
    @InjectMocks
    private UserServiceImpl testingInstance;

    // Тесты
    // POSITIVE TESTS

    @Test
    void shouldLoadUserByUsername() {
        // Given
        User user = getUser();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        // When
        UserDetails result = testingInstance.loadUserByUsername(USERNAME);
        // Then
        verify(userDao).findByUsername(USERNAME);
        assertEquals(USERNAME, result.getUsername());
        assertEquals(PASSWORD, result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(ROLE_READER)));
    }

    @Test
    void shouldRegisterNewUser() {
        // Given
        User user = getUser();
        user.setPassword(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        // When
        testingInstance.register(user);
        // Then
        verify(userDao).save(user);
        verify(passwordEncoder).encode(PASSWORD);
        assertEquals(ENCODED_PASSWORD, user.getPassword());
        assertEquals(Role.READER, user.getRole());
        assertEquals(ACTIVE, user.getStatus());
    }

    @Test
    void shouldUpdateUser() {
        // Given
        UserDto userDto = getUserDto(USER_ID, NEW_MAIL_COM, Role.ADMIN, BLOCKED);
        User user = getUser();
        user.setPassword(OLD);
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        // When
        testingInstance.updateUser(userDto, PASSWORD);
        // Then
        verify(userDao).update(user);
        assertEquals(NEW_MAIL_COM, user.getEmail());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(BLOCKED, user.getStatus());
        assertEquals(ENCODED_PASSWORD, user.getPassword());
    }

    @Test
    void shouldUpdateUserButDoesNotUpdatePasswordWhenItIsNotProvided() {
        // Given
        UserDto userDto = getUserDto(USER_ID, NEW_MAIL_COM, Role.READER, ACTIVE);
        User user = getUser();
        user.setPassword(OLD_PASS);
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(user));
        // When
        testingInstance.updateUser(userDto, SPACES);
        // Then
        verify(userDao).update(user);
        assertEquals(OLD_PASS, user.getPassword());
    }

    @Test
    void shouldGetUserByUsername() {
        // Given
        User user = getUser();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        // When
        User result = testingInstance.getUsername(USERNAME);
        // Then
        verify(userDao).findByUsername(USERNAME);
        assertEquals(user, result);
    }

    @Test
    void shouldGetUserById() {
        // Given
        User user = getUser();
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(user));
        // When
        Optional<User> result = testingInstance.getById(USER_ID);
        // Then
        verify(userDao).findById(USER_ID);
        assertTrue(result.isPresent());
    }

    @Test
    void shouldDeleteUser() {
        // When
        testingInstance.deleteUser(USER_ID);
        // Then
        verify(userDao).delete(USER_ID);
    }

    @Test
    void shouldCountUsersByStatuses() {
        // Given
        when(userDao.countUserByStatus(BLOCKED)).thenReturn(3L);
        // When
        long count = testingInstance.countByStatus(BLOCKED);
        // Then
        verify(userDao).countUserByStatus(BLOCKED);
        assertEquals(3L, count);
    }

    @Test
    void shouldGetAllUserDtos() {
        // Given
        User user = getUser();
        when(userDao.findAll()).thenReturn(List.of(user));
        // When
        List<UserDto> result = testingInstance.getAllUserDtos();
        // Then
        verify(userDao).findAll();
        assertEquals(1, result.size());
        assertEquals(USER_ID, result.get(0).getId());
    }

    @Test
    void shouldGetUserDtoById() {
        // Given
        User user = getUser();
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(user));
        // When
        Optional<UserDto> result = testingInstance.getDtoById(USER_ID);
        // Then
        verify(userDao).findById(USER_ID);
        assertTrue(result.isPresent());
        assertEquals(USER_ID, result.get().getId());
    }

    @Test
    void shouldGetDtoByUsername() {
        // Given
        User user = getUser();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        // When
        Optional<UserDto> result = testingInstance.getDtoByUsername(USERNAME);
        // Then
        verify(userDao).findByUsername(USERNAME);
        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get().getUsername());
    }

    @Test
    void shouldGetReadersWithActiveOrders() {
        // Given
        User user = getUser();
        Order order = new Order();
        Map<User, List<Order>> expectedMap = Map.of(user, List.of(order));
        when(userDao.findReadersWithActiveOrders()).thenReturn(expectedMap);
        // When
        Map<User, List<Order>> result = testingInstance.getReadersWithActiveOrders();
        // Then
        verify(userDao).findReadersWithActiveOrders();
        assertEquals(expectedMap, result);
    }

    // NEGATIVE TESTS

    @Test
    void shouldNotLoadUserByUsernameWhenUserNotFound() {
        // Given
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        // Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> testingInstance.loadUserByUsername(USERNAME)
        );
        verify(userDao).findByUsername(USERNAME);
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void shouldNotUpdateUserWhenUserNotFound() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(USER_ID);
        when(userDao.findById(USER_ID)).thenReturn(Optional.empty());
        // Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                testingInstance.updateUser(userDto, PASSWORD));
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void shouldNotGetUserByUsernameWhenUserNotFound() {
        // Given
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        // When & Then
        assertThrows(UsernameNotFoundException.class,
                () -> testingInstance.getUsername(USERNAME));
    }

    @Test
    void shouldNotRegisterNewUserWhenUserIsNull() {
        // Then
        assertThrows(NullPointerException.class, () -> testingInstance.register(null));
    }

    @Test
    void shouldNotUpdateUserWhenDtoIsNull() {
        // Then
        assertThrows(NullPointerException.class, () -> testingInstance.updateUser(null, PASSWORD));
    }

    @Test
    void shouldNotUpdateUserWhenPasswordIsNullOrBlank() {
        // Given
        UserDto dto = getUserDto(USER_ID, NEW_MAIL_COM, Role.READER, ACTIVE);
        User user = getUser();
        user.setPassword(OLD_PASS);
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(user));

        // When
        testingInstance.updateUser(dto, "   ");
        // Then
        verify(userDao).update(user);
        assertEquals(OLD_PASS, user.getPassword());
    }

    @Test
    void shouldNotGetUserDtoByIdWhenUserNotFound() {
        // Given
        when(userDao.findById(USER_ID)).thenReturn(Optional.empty());
        // When
        Optional<UserDto> result = testingInstance.getDtoById(USER_ID);
        // Then
        verify(userDao).findById(USER_ID);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotGetDtoByUsernameWhenUserNotFound() {
        // Given
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        // When
        Optional<UserDto> result = testingInstance.getDtoByUsername(USERNAME);
        // Then
        verify(userDao).findByUsername(USERNAME);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotGetUsernameWhenUsernameIsNull() {
        // Then
        assertThrows(UsernameNotFoundException.class, () -> testingInstance.getUsername(null));
    }

    @Test
    void shouldNotLoadUserByUsernameWhenUsernameIsNull() {
        // Then
        assertThrows(UsernameNotFoundException.class, () -> testingInstance.loadUserByUsername(null));
    }

    private static User getUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user.setRole(Role.READER);
        user.setStatus(ACTIVE);

        return user;
    }

    private static UserDto getUserDto(Long id, String email, Role role, String status) {
        UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setEmail(email);
        userDto.setRole(role);
        userDto.setStatus(status);

        return userDto;
    }
}
