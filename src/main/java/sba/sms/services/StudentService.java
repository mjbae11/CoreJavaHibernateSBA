package sba.sms.services;

import org.hibernate.Session;
import org.hibernate.Transaction;
import sba.sms.dao.StudentI;
import sba.sms.models.Course;
import sba.sms.models.Student;
import sba.sms.utils.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * StudentService is a concrete class. This class implements the
 * StudentI interface, overrides all abstract service methods and
 * provides implementation for each method. Lombok @Log used to
 * generate a logger file.
 */

public class StudentService implements StudentI {

    @Override
    public void createStudent(Student student)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession())
        {
            Transaction transaction = session.beginTransaction();
            try
            {
                if (student == null)
                {
                    throw new RuntimeException("Could not find student.");
                }
                session.save(student);
                transaction.commit();
            } catch (Exception e)
            {
                if (transaction != null && transaction.isActive())
                {
                    transaction.rollback();
                }
                throw new RuntimeException("Failed to create student", e);
            }
        }
    }

    @Override
    public List<Student> getAllStudents()
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession())
        {
            return session.createQuery("from Student", Student.class).list();
        }
    }

    @Override
    public Student getStudentByEmail(String email)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession())
        {
            return session.get(Student.class, email);
        }
    }

    @Override
    public boolean validateStudent(String email, String password)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession())
        {
            Student student = session.get(Student.class, email);
            return (student != null && student.getPassword().equals(password));
        }
    }

    @Override
    public void registerStudentToCourse(String email, int courseId)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession())
        {
            Transaction transaction = session.beginTransaction();
            try
            {
                Course course = session.get(Course.class, courseId);
                Student student = session.get(Student.class, email);
                if (course == null)
                {
                    throw new RuntimeException("Course Not found with ID: "+ courseId);
                }
                if (student == null)
                {
                    throw new RuntimeException("Student not found with email: " + email);
                }
                if (course.getStudents().contains(student))
                {
                    throw new RuntimeException("Student already registered");
                }

                course.getStudents().add(student);
                student.getCourses().add(course);
                session.merge(course);
                session.merge(student);

                transaction.commit();
            } catch (Exception e)
            {
                if (transaction != null && transaction.isActive())
                {
                    transaction.rollback();
                }
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<Course> getStudentCourses(String email)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession())
        {
            Student student = session.get(Student.class, email);
            return new ArrayList<>(student.getCourses());
        }
    }
}
