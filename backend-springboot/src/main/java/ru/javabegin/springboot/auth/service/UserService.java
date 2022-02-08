package ru.javabegin.springboot.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.javabegin.springboot.auth.entity.Activity;
import ru.javabegin.springboot.auth.entity.Role;
import ru.javabegin.springboot.auth.entity.User;
import ru.javabegin.springboot.auth.repository.ActivityRepository;
import ru.javabegin.springboot.auth.repository.RoleRepository;
import ru.javabegin.springboot.auth.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.Optional;

@Service

/*
 @Transactional  будет применяться к каждому методу сервиса.
 Пригодится на будущее, если в одном методе будет неск. вызовов в БД - все будут выполняться в одной транзакции.
 Можно будет настраивать транзакции точечно по необходимости.
 Если в методе при вызове репозитория возникнет исключение - все выполненные вызовы к БД из данного метода откатятся (Rollback)
*/
@Transactional


public class UserService {

    public static final String DEFAULT_ROLE = "USER"; // такая роль должна быть обязательно в таблице БД


    private UserRepository userRepository; // работа с пользователями
    private RoleRepository roleRepository; // работа с ролями
    private ActivityRepository activityRepository; // работа с активностями


    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.activityRepository = activityRepository;
    }


    public void register(User user, Activity activity) {

        // сохраняем данные в БД - если будет ошибка - никакие данные в БД не попадут, произойдет Rollback (откат транзакции) - благодаря @Transactional
        userRepository.save(user);
        activityRepository.save(activity); // почему мы отдельно сохр. activity - потому это новый пользователь и у него еще нет соттв. записи в Activity

        // даже если в последнем вызове репозитория выйдет ошибка - все предыдущие вызовы также откатятся
    }

//    public void save(User user) {
//        userRepository.save(user);
//    }



    // проверка, существует ли пользователь в БД (email и username должны быть уникальными в таблице)
    public boolean userExists(String username, String email) {

        if (userRepository.existsByUsername(username)) {
            return true; // если запись в БД существует
        }

        if (userRepository.existsByEmail(email)) {
            return true; // если запись в БД существует
        }

        return false;
    }

    // получаем из БД объект роли
    public Optional<Role> findByName(String role) {
        return roleRepository.findByName(role);
    }

    public Activity saveActivity(Activity activity){
        return activityRepository.save(activity);
    }


    public Optional<Activity> findActivityByUserId(long id){
        return activityRepository.findByUserId(id);
    }

    public Optional<Activity> findActivityByUuid(String uuid){
        return activityRepository.findByUuid(uuid);
    }

    // true сконвертируется в 1, т.к. указали @Type(type = "org.hibernate.type.NumericBooleanType") в классе Activity
    public int activate(String uuid){
        return activityRepository.changeActivated(uuid, true);
    }

    // false сконвертируется в 0, т.к. указали @Type(type = "org.hibernate.type.NumericBooleanType") в классе Activity
    public int deactivate(String uuid){
        return activityRepository.changeActivated(uuid, false);
    }


    public int updatePassword(String password, String email){
        return userRepository.updatePassword(password, email);
    }

}
