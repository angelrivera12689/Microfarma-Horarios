package MicrofarmaHorarios.News.Controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.News.Entity.News;
import MicrofarmaHorarios.News.Entity.NewsType;
import MicrofarmaHorarios.News.IService.INewsNewsService;

@WebMvcTest(NewsNewsController.class)
public class NewsNewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private INewsNewsService service;

    @Autowired
    private ObjectMapper objectMapper;

    private News news;
    private NewsType newsType;
    private Employee employee;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void testFindByStateTrue() throws Exception {
        List<News> newsList = Arrays.asList(news);
        when(service.findByStateTrue()).thenReturn(newsList);

        mockMvc.perform(get("/api/news/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Test News"));
    }

    @Test
    void testShow() throws Exception {
        when(service.findById("1")).thenReturn(Optional.of(news));

        mockMvc.perform(get("/api/news/news/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void testShowNotFound() throws Exception {
        when(service.findById("1")).thenThrow(new Exception("Registro no encontrado"));

        mockMvc.perform(get("/api/news/news/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSave() throws Exception {
        when(service.save(any(News.class))).thenReturn(news);

        mockMvc.perform(post("/api/news/news")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(news)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void testUpdate() throws Exception {
        doNothing().when(service).update(eq("1"), any(News.class));

        mockMvc.perform(put("/api/news/news/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(news)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void testDelete() throws Exception {
        doNothing().when(service).delete("1");

        mockMvc.perform(delete("/api/news/news/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }
}