package com.mitrais.bootcamp.rms;

import com.mitrais.bootcamp.rms.data.constanta.EmployeeStatus;
import com.mitrais.bootcamp.rms.data.constanta.Gender;
import com.mitrais.bootcamp.rms.data.entity.Employee;
import com.mitrais.bootcamp.rms.data.repository.EmployeeRepository;
import com.mitrais.bootcamp.rms.data.web.FilterDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;

import static java.lang.Math.toIntExact;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MediaType halContentType = new MediaType(MediaTypes.HAL_JSON, Charset.forName("utf8"));
    private MediaType jsonContentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmployeeRepository employeeRepository;

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        this.employeeRepository.deleteAll();
    }

    @Test
    public void addEmployee() throws Exception {

        Employee newEmployee = new Employee();
        newEmployee.setFirstName("employee");
        newEmployee.setLastName("1");
        newEmployee.setPhone("+621");
        newEmployee.setEmail("employee.1@mitrais.com");
        newEmployee.setGender(Gender.Male);
        newEmployee.setEmpStatus(EmployeeStatus.Contract);
        newEmployee.setJobFamily("SE");
        newEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));

        mockMvc.perform(post("/employees")
                .contentType(jsonContentType)
                .content(this.json(newEmployee)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/employees/filter?sort=dateAdded,desc")
                .contentType(jsonContentType)
                .content(this.json(new FilterDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(1)))
                .andExpect(jsonPath("$._embedded.employees", hasSize(1)))
                .andExpect(jsonPath("$._embedded.employees[0].empId", notNullValue()))
                .andExpect(jsonPath("$._embedded.employees[0].firstName", equalToIgnoringCase(newEmployee.getFirstName())))
                .andExpect(jsonPath("$._embedded.employees[0].lastName", equalToIgnoringCase(newEmployee.getLastName())))
                .andExpect(jsonPath("$._embedded.employees[0].phone", equalToIgnoringCase(newEmployee.getPhone())))
                .andExpect(jsonPath("$._embedded.employees[0].gender", equalToIgnoringCase(newEmployee.getGender().name())))
                .andExpect(jsonPath("$._embedded.employees[0].empStatus", equalToIgnoringCase(newEmployee.getEmpStatus().name())))
                .andExpect(jsonPath("$._embedded.employees[0].jobFamily", equalToIgnoringCase(newEmployee.getJobFamily())))
                .andExpect(jsonPath("$._embedded.employees[0].hiredDate", equalToIgnoringCase(newEmployee.getHiredDate().toString())));
    }

    @Test
    public void addEmployeeDuplicatePhone() throws Exception {

        Employee newEmployee = new Employee();
        newEmployee.setFirstName("employee");
        newEmployee.setLastName("1");
        newEmployee.setPhone("+621");
        newEmployee.setEmail("employee.1@mitrais.com");
        newEmployee.setGender(Gender.Male);
        newEmployee.setEmpStatus(EmployeeStatus.Contract);
        newEmployee.setJobFamily("SE");
        newEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));

        mockMvc.perform(post("/employees")
                .contentType(jsonContentType)
                .content(this.json(newEmployee)))
                .andExpect(status().isCreated());

        Employee secondEmployee = new Employee();
        secondEmployee.setFirstName("employee");
        secondEmployee.setLastName("2");
        secondEmployee.setPhone("+621");
        secondEmployee.setEmail("employee.2@mitrais.com");
        secondEmployee.setGender(Gender.Female);
        secondEmployee.setEmpStatus(EmployeeStatus.Permanent);
        secondEmployee.setJobFamily("SE");
        secondEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));


        mockMvc.perform(post("/employees")
                .contentType(jsonContentType)
                .content(this.json(secondEmployee)))
                .andExpect(status().isConflict());
    }

    @Test
    public void addEmployeeDuplicateEmail() throws Exception {

        Employee newEmployee = new Employee();
        newEmployee.setFirstName("employee");
        newEmployee.setLastName("1");
        newEmployee.setPhone("+621");
        newEmployee.setEmail("employee.1@mitrais.com");
        newEmployee.setGender(Gender.Male);
        newEmployee.setEmpStatus(EmployeeStatus.Contract);
        newEmployee.setJobFamily("SE");
        newEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));

        mockMvc.perform(post("/employees")
                .contentType(jsonContentType)
                .content(this.json(newEmployee)))
                .andExpect(status().isCreated());

        Employee secondEmployee = new Employee();
        secondEmployee.setFirstName("employee");
        secondEmployee.setLastName("2");
        secondEmployee.setPhone("+622");
        secondEmployee.setEmail("employee.1@mitrais.com");
        secondEmployee.setGender(Gender.Female);
        secondEmployee.setEmpStatus(EmployeeStatus.Permanent);
        secondEmployee.setJobFamily("SE");
        secondEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));


        mockMvc.perform(post("/employees")
                .contentType(jsonContentType)
                .content(this.json(secondEmployee)))
                .andExpect(status().isConflict());
    }

    @Test
    public void getSingleEmployee() throws Exception {

        Employee newEmployee = new Employee();
        newEmployee.setFirstName("employee");
        newEmployee.setLastName("1");
        newEmployee.setPhone("+621");
        newEmployee.setEmail("employee.1@mitrais.com");
        newEmployee.setGender(Gender.Male);
        newEmployee.setEmpStatus(EmployeeStatus.Contract);
        newEmployee.setJobFamily("SE");
        newEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setDateAdded(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setLastModified(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        Employee savedEmployee = employeeRepository.save(newEmployee);

        mockMvc.perform(get("/employees/"+savedEmployee.getEmpId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Etag", equalToIgnoringCase("\"0\"")))
                .andExpect(jsonPath("$.empId", comparesEqualTo(toIntExact(savedEmployee.getEmpId()))))
                .andExpect(jsonPath("$.firstName", equalToIgnoringCase(newEmployee.getFirstName())))
                .andExpect(jsonPath("$.lastName", equalToIgnoringCase(newEmployee.getLastName())))
                .andExpect(jsonPath("$.phone", equalToIgnoringCase(newEmployee.getPhone())))
                .andExpect(jsonPath("$.gender", equalToIgnoringCase(newEmployee.getGender().name())))
                .andExpect(jsonPath("$.empStatus", equalToIgnoringCase(newEmployee.getEmpStatus().name())))
                .andExpect(jsonPath("$.jobFamily", equalToIgnoringCase(newEmployee.getJobFamily())))
                .andExpect(jsonPath("$.hiredDate", equalToIgnoringCase(newEmployee.getHiredDate().toString())));
    }

    @Test
    public void patchEmployee() throws Exception {

        Employee newEmployee = new Employee();
        newEmployee.setFirstName("employee");
        newEmployee.setLastName("1");
        newEmployee.setPhone("+621");
        newEmployee.setEmail("employee.1@mitrais.com");
        newEmployee.setGender(Gender.Male);
        newEmployee.setEmpStatus(EmployeeStatus.Contract);
        newEmployee.setJobFamily("SE");
        newEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setDateAdded(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setLastModified(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        Employee savedEmployee = employeeRepository.save(newEmployee);

        newEmployee.setLastName("updated");

        mockMvc.perform(patch("/employees/"+savedEmployee.getEmpId())
                .contentType(jsonContentType)
                .header("If-Match", "\"0\"")
                .content(this.json(newEmployee)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/employees/"+savedEmployee.getEmpId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Etag", equalToIgnoringCase("\"1\"")))
                .andExpect(jsonPath("$.empId", comparesEqualTo(toIntExact(savedEmployee.getEmpId()))))
                .andExpect(jsonPath("$.firstName", equalToIgnoringCase(newEmployee.getFirstName())))
                .andExpect(jsonPath("$.lastName", equalToIgnoringCase(newEmployee.getLastName())))
                .andExpect(jsonPath("$.phone", equalToIgnoringCase(newEmployee.getPhone())))
                .andExpect(jsonPath("$.gender", equalToIgnoringCase(newEmployee.getGender().name())))
                .andExpect(jsonPath("$.empStatus", equalToIgnoringCase(newEmployee.getEmpStatus().name())))
                .andExpect(jsonPath("$.jobFamily", equalToIgnoringCase(newEmployee.getJobFamily())))
                .andExpect(jsonPath("$.hiredDate", equalToIgnoringCase(newEmployee.getHiredDate().toString())));
    }

    @Test
    public void patchEmployeeErrorEtag() throws Exception {

        Employee newEmployee = new Employee();
        newEmployee.setFirstName("employee");
        newEmployee.setLastName("1");
        newEmployee.setPhone("+621");
        newEmployee.setEmail("employee.1@mitrais.com");
        newEmployee.setGender(Gender.Male);
        newEmployee.setEmpStatus(EmployeeStatus.Contract);
        newEmployee.setJobFamily("SE");
        newEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setDateAdded(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setLastModified(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        Employee savedEmployee = employeeRepository.save(newEmployee);

        newEmployee.setLastName("updated");

        savedEmployee = employeeRepository.save(newEmployee);

        mockMvc.perform(patch("/employees/"+savedEmployee.getEmpId())
                .contentType(jsonContentType)
                .header("If-Match", "\"0\"")
                .content(this.json(newEmployee)))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    public void searchByName() throws Exception {

        Employee newEmployee = new Employee();
        newEmployee.setFirstName("Cal");
        newEmployee.setLastName("Supreme");
        newEmployee.setPhone("+621");
        newEmployee.setEmail("employee.1@mitrais.com");
        newEmployee.setGender(Gender.Male);
        newEmployee.setEmpStatus(EmployeeStatus.Contract);
        newEmployee.setJobFamily("SE");
        newEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setDateAdded(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setLastModified(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        Employee savedEmployee = employeeRepository.save(newEmployee);

        Employee secondEmployee = new Employee();
        secondEmployee.setFirstName("Cal");
        secondEmployee.setLastName("Superman");
        secondEmployee.setPhone("+622");
        secondEmployee.setEmail("employee.2@mitrais.com");
        secondEmployee.setGender(Gender.Male);
        secondEmployee.setEmpStatus(EmployeeStatus.Permanent);
        secondEmployee.setJobFamily("SE");
        secondEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));
        secondEmployee.setDateAdded(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        secondEmployee.setLastModified(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        savedEmployee = employeeRepository.save(secondEmployee);

        mockMvc.perform(get("/employees/search/findByName?name=cal sup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(2)))
                .andExpect(jsonPath("$._embedded.employees", hasSize(2)));
    }

    @Test
    public void searchByNameEmptyParam() throws Exception {

        Employee newEmployee = new Employee();
        newEmployee.setFirstName("Cal");
        newEmployee.setLastName("Supreme");
        newEmployee.setPhone("+621");
        newEmployee.setEmail("employee.1@mitrais.com");
        newEmployee.setGender(Gender.Male);
        newEmployee.setEmpStatus(EmployeeStatus.Contract);
        newEmployee.setJobFamily("SE");
        newEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setDateAdded(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setLastModified(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        Employee savedEmployee = employeeRepository.save(newEmployee);

        Employee secondEmployee = new Employee();
        secondEmployee.setFirstName("Cal");
        secondEmployee.setLastName("Superman");
        secondEmployee.setPhone("+622");
        secondEmployee.setEmail("employee.2@mitrais.com");
        secondEmployee.setGender(Gender.Male);
        secondEmployee.setEmpStatus(EmployeeStatus.Permanent);
        secondEmployee.setJobFamily("SE");
        secondEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));
        secondEmployee.setDateAdded(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        secondEmployee.setLastModified(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        savedEmployee = employeeRepository.save(secondEmployee);

        mockMvc.perform(get("/employees/search/findByName?name="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(2)))
                .andExpect(jsonPath("$._embedded.employees", hasSize(2)));
    }

    @Test
    public void searchByNameEmptyResult() throws Exception {

        Employee newEmployee = new Employee();
        newEmployee.setFirstName("Cal");
        newEmployee.setLastName("Supreme");
        newEmployee.setPhone("+621");
        newEmployee.setEmail("employee.1@mitrais.com");
        newEmployee.setGender(Gender.Male);
        newEmployee.setEmpStatus(EmployeeStatus.Contract);
        newEmployee.setJobFamily("SE");
        newEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setDateAdded(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        newEmployee.setLastModified(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        Employee savedEmployee = employeeRepository.save(newEmployee);

        Employee secondEmployee = new Employee();
        secondEmployee.setFirstName("Cal");
        secondEmployee.setLastName("Superman");
        secondEmployee.setPhone("+622");
        secondEmployee.setEmail("employee.2@mitrais.com");
        secondEmployee.setGender(Gender.Male);
        secondEmployee.setEmpStatus(EmployeeStatus.Permanent);
        secondEmployee.setJobFamily("SE");
        secondEmployee.setHiredDate(new Date(Calendar.getInstance().getTimeInMillis()));
        secondEmployee.setDateAdded(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        secondEmployee.setLastModified(new Timestamp(Calendar.getInstance().getTimeInMillis()));

        savedEmployee = employeeRepository.save(secondEmployee);

        mockMvc.perform(get("/employees/search/findByName?name=employee panjang namanya"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(0)))
                .andExpect(jsonPath("$._embedded.employees", hasSize(0)));
    }
}
