import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
    public static void main(String[] args) {
        RestTemplate restTemplate = restTemplate();

        String ip = null;
        String port = "8080";

        if (args.length == 1) {
            ip = args[0];
        }
        else {
            System.out.println("Ip is missing from program arguments. Ex: java -jar EventArchiveConsumer.jar 192.168.56.105");
            System.exit(-1);
        }

        String url = "http://" + ip + ":8080/monitoring/events";

        ResponseEntity<PagedResources<EventHubMessage>> responseEntity = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<PagedResources<EventHubMessage>>() {},
                port, 0, 100);
        PagedResources<EventHubMessage> resources = responseEntity.getBody();
        List<EventHubMessage> events = new ArrayList(resources.getContent());
        for (EventHubMessage event : events) {
            JSONObject json = new JSONObject(event.toString());
            System.out.println(json.toString(4));
        }
    }

    private static RestTemplate restTemplate() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(mapper);
        return new RestTemplate(Arrays.asList(converter));
    }

}
