package MicrofarmaHorarios.News.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.News.Entity.News;
import MicrofarmaHorarios.News.Entity.NewsType;
import MicrofarmaHorarios.News.IRepository.INewsNewsRepository;

public class NewsNewsServiceTest {

    @Mock
    private INewsNewsRepository repository;

    @InjectMocks
    private NewsNewsService service;

    private News news;
    private NewsType newsType;
    private Employee employee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        newsType = new NewsType();
        newsType.setId("1");
        newsType.setName("General");

        employee = new Employee();
        employee.setId("1");
        employee.setFirstName("John");
        employee.setLastName("Doe");

        news = new News();
        news.setId("1");
        news.setTitle("Test News");
        news.setContent("Test Content");
        news.setPublicationDate(LocalDate.now());
        news.setNewsType(newsType);
        news.setEmployee(employee);
        news.setStatus(true);
    }

    @Test
    void testFindByStateTrue() throws Exception {
        List<News> newsList = Arrays.asList(news);
        when(repository.findAll()).thenReturn(newsList);

        List<News> result = service.findByStateTrue();

        assertEquals(1, result.size());
        assertEquals("Test News", result.get(0).getTitle());
    }

    @Test
    void testFindById() throws Exception {
        when(repository.findById("1")).thenReturn(Optional.of(news));

        Optional<News> result = service.findById("1");

        assertTrue(result.isPresent());
        assertEquals("Test News", result.get().getTitle());
    }

    @Test
    void testFindByIdNotFound() throws Exception {
        when(repository.findById("1")).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> service.findById("1"));
        assertEquals("Registro no encontrado", exception.getMessage());
    }

    @Test
    void testSave() throws Exception {
        when(repository.save(any(News.class))).thenReturn(news);

        News result = service.save(news);

        assertNotNull(result);
        assertEquals("Test News", result.getTitle());
        verify(repository).save(news);
    }

    @Test
    void testUpdate() throws Exception {
        when(repository.findById("1")).thenReturn(Optional.of(news));
        when(repository.save(any(News.class))).thenReturn(news);

        service.update("1", news);

        verify(repository).save(news);
    }

    @Test
    void testUpdateNotFound() throws Exception {
        when(repository.findById("1")).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> service.update("1", news));
        assertEquals("Registro no encontrado", exception.getMessage());
    }

    @Test
    void testDelete() throws Exception {
        when(repository.findById("1")).thenReturn(Optional.of(news));
        when(repository.save(any(News.class))).thenReturn(news);

        service.delete("1");

        verify(repository).save(news);
    }
}