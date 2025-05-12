package org.example;

import java.util.*;
import java.util.stream.Collectors;


class Employee {
    private String name;
    private String department;
    private double salary;
    private int age;

    public Employee(String name, String department, double salary, int age) {
        this.name = name;
        this.department = department;
        this.salary = salary;
        this.age = age;
    }

    public String getName() {
        return name;
    }


    public String getDepartment() {
        return department;
    }


    public double getSalary() {
        return salary;
    }

    public int getAge() {
        return age;
    }

    public String toString() {
        return name + " (" + department + ", $" + salary + ", " + age + " лет)";
    }
}


class StreamApiTask {
    public static Map<String, List<Employee>> foo(List<Employee> employee) {
        // Ваше решение
        return employee.stream()
                .filter(it -> it.getAge() > 30 && it.getSalary() > 50_000)
                .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
                .collect(Collectors.groupingBy(Employee::getDepartment, LinkedHashMap::new, Collectors.toList()));
    }
}

class Runner {
    public static void main(String[] args) {
        List employees = Arrays.asList(
                new Employee("John", "IT", 60000, 35),
                new Employee("Alice", "HR", 70000, 40),
                new Employee("Bob", "IT", 50000, 25),
                new Employee("Eve", "Finance", 80000, 45),
                new Employee("Charlie", "HR", 70000, 32),
                new Employee("Dave", "Finance", 55000, 38)
        );
        Map<String, List<Employee>> employeeMap =  StreamApiTask.foo(employees);
        printResult(employeeMap);
    }

    private static void printResult(Map result) {
        result.forEach((department, employees) -> {
            System.out.println("Отдел " + department + ": " + employees);
        });
    }
}