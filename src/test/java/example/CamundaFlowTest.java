package example;

import example.bootstrap.CamundaConfiguration;
import java.io.InputStream;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CamundaConfiguration.class})
public class CamundaFlowTest {

    private final static String MODEL_NAME = "CamundaExample.bpmn";
    private final ProcessScenario processScenario = mock(ProcessScenario.class);

    @Autowired
    public ProcessEngine processEngine;

    @Before
    public void deployProcess() {

        final InputStream is = CamundaFlowTest.class.getClassLoader().getResourceAsStream(MODEL_NAME);
        final BpmnModelInstance flow = Bpmn.readModelFromStream(is);

        final RepositoryService repositoryService = processEngine.getRepositoryService();
        repositoryService.createDeployment()
                    .addModelInstance(MODEL_NAME, flow)
                    .deploy();
    }

    @After
    public void resetMocks() {
        Mocks.reset();
    }

    private ProcessInstance startProcess(final boolean throwException) {

        final VariableMap variables = Variables.createVariables()
                .putValue("THROW_ERROR", throwException);

        final Scenario scenario = Scenario.run(this.processScenario)
                .startByKey("CamundaExample", variables)
                .execute();

        return scenario.instance(this.processScenario);
    }

    @Test
    public void happyFlow() {

        final ProcessInstance processInstance = startProcess(false);

        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasPassedInOrder(
                "start_event",
                "call_a",
                "call_b",
                "send_audit_msg_ok",
                "audit",
                "end_event");
    }

    @Test
    public void errorFlow() {

        final ProcessInstance processInstance = startProcess(true);

        assertThat(processInstance).isEnded();
        assertThat(processInstance).hasPassedInOrder(
                "start_event",
                "call_a",
                "call_b",
                "throw_error",
                "compensate_b",
                "compensate_a",      // TODO: not executed, why?
                "handle_error",
                "send_audit_msg_ko", // TODO: exception is throw, check logs
                "audit",
                "end_event");
    }
}
