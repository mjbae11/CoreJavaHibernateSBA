package sba.sms.services;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sba.sms.models.Course;
import sba.sms.models.Student;
import sba.sms.utils.CommandLine;
import sba.sms.utils.HibernateUtil;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


class StudentServiceTest
{

    private StudentService studentService;
    private CourseService courseService;

    @BeforeAll
    static void setUp()
    {
        CommandLine.addData();
    }

    @BeforeEach
    void init()
    {
        studentService = new StudentService();
        courseService = new CourseService();
        Student student = new Student("test@test.com", "Test Student", "password");
        studentService.createStudent(student);
    }

    @Test
    void registerStudentToCourse_ShouldRegisterSuccessfully()
    {
        String testEmail = "test@test.com";
        int courseId = 1;
        studentService.registerStudentToCourse(testEmail, courseId);

        Student student = studentService.getStudentByEmail(testEmail);
        List<Course> courses = new ArrayList<>(student.getCourses());
        boolean containsCourse = false;
        for (Course course : courses)
        {
            if (course.getId() == courseId)
            {
                containsCourse = true;
                break;
            }
        }
        assertThat(containsCourse).isTrue();
    }

    @Test
    void registerStudentToCourse_ShouldThrowException_WhenCourseNotFound()
    {
        String studentEmail = "test@test.com";
        int nonExistentCourseId = 999;

        try
        {
            studentService.registerStudentToCourse(studentEmail, nonExistentCourseId);
            fail("Expected RuntimeException was not thrown");
        } catch (RuntimeException e)
        {
            assertThat(e.getMessage()).contains("Course Not found");
        }
    }

    @Test
    void registerStudentToCourse_ShouldThrowException_WhenStudentNotFound()
    {
        String nonExistentEmail = "notfound@test.com";
        int courseId = 1;

        try
        {
            studentService.registerStudentToCourse(nonExistentEmail, courseId);
            fail("Expected RuntimeException was not thrown");
        } catch (RuntimeException e)
        {
            assertThat(e.getMessage()).contains("Student not found with email: " + nonExistentEmail);
        }
    }

    @Test
    void registerStudentToCourse_ShouldThrowException_WhenAlreadyRegistered()
    {
        String studentEmail = "test@test.com";
        int courseId = 1;
        // first working register
        studentService.registerStudentToCourse(studentEmail, courseId);
        // second register should not work
        try
        {
            studentService.registerStudentToCourse(studentEmail, courseId);
            fail("Expected RuntimeException was not thrown");
        } catch (RuntimeException e)
        {
            assertThat(e.getMessage()).contains("Student already registered");
        }
    }

    @AfterAll
    static void tearDown()
    {
        HibernateUtil.shutdown();
    }
}
