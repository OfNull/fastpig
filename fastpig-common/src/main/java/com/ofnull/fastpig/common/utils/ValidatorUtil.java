package com.ofnull.fastpig.common.utils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author ofnull
 * @date 2022/2/15 17:41
 */
public class ValidatorUtil {
    private static Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static void validate(Object o) {
        Set<ConstraintViolation<Object>> set = validator.validate(o);
        if (set != null && !set.isEmpty()) {
            for (ConstraintViolation<Object> constraintViolation : set) {
                throw new IllegalArgumentException(o.getClass() + " validate Exception!  " + constraintViolation.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Car car = new Car(null, 19);
        ValidatorUtil.validate(car);
    }

    static class Car {
        @NotNull(message = "name is not  null")
        String name;
        Integer age;

        public Car(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
