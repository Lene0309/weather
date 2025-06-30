package edu.neu.weather.activity.city;

import java.io.Serializable;
import java.util.Objects;

public class City implements Serializable {
    private final String name;
    private final String code;

    public City(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return name.equals(city.name) && code.equals(city.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code);
    }
}
