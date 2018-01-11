package org;

import java.time.LocalDate;

public class Account  {

    public Account(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public enum Sex {
        Male, Female;

        @Override
        public String toString() {
            return this == Male ? "Male" : "Female";
        }
    };

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Sex getSex() {
        return sex;
    }

    public boolean equals(Account a) {
        return name.equals(a.name) &&
                login.equals(a.login) &&
                password.equals(a.password) &&
                birthday.equals(a.birthday) &&
                sex == a.sex;
    }

    String name;
    String login;
    String password;
    LocalDate birthday;
    Sex sex;
}
