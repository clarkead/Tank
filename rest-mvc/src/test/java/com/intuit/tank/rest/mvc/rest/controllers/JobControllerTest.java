/**
 *  Copyright 2015-2023 Intuit Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */
package com.intuit.tank.rest.mvc.rest.controllers;

import com.intuit.tank.api.model.v1.cloud.CloudVmStatusContainer;
import com.intuit.tank.api.model.v1.cloud.CloudVmStatus;
import com.intuit.tank.rest.mvc.rest.services.jobs.JobServiceV2;
import com.intuit.tank.rest.mvc.rest.models.jobs.CreateJobRequest;
import com.intuit.tank.rest.mvc.rest.models.jobs.JobContainer;
import com.intuit.tank.rest.mvc.rest.models.jobs.JobTO;
import com.intuit.tank.vm.agent.messages.Headers;
import com.intuit.tank.vm.api.enumerated.JobStatus;
import com.intuit.tank.vm.api.enumerated.VMImageType;
import com.intuit.tank.vm.api.enumerated.VMRegion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobControllerTest {
    @InjectMocks
    private JobController jobController;

    @Mock
    private JobServiceV2 jobService;

    @Mock
    HttpServletRequest request;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void testGetPing() {
        when(jobService.ping()).thenReturn("PONG");
        ResponseEntity<String> result = jobController.ping();
        assertEquals("PONG", result.getBody());
        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    public void testGetJobs() {
        List<JobTO> jobs = new ArrayList<>();
        JobTO testJob = new JobTO();
        testJob.setId(5);
        testJob.setName("testJobName");
        testJob.setStatus("testJobStatus");

        jobs.add(testJob);
        JobContainer jobContainer = new JobContainer(jobs);
        when(jobService.getAllJobs()).thenReturn(jobContainer);

        ResponseEntity<JobContainer> result = jobController.getAllJobs();
        assertEquals(5, result.getBody().getJobs().get(0).getId());
        assertEquals("testJobName", result.getBody().getJobs().get(0).getName());
        assertEquals("testJobStatus", result.getBody().getJobs().get(0).getStatus());
        assertEquals(200, result.getStatusCodeValue());
        verify(jobService).getAllJobs();
    }

    @Test
    public void testGetJob() {
        JobTO testJob = new JobTO();
        testJob.setId(9);
        testJob.setName("testSingleJob");
        testJob.setStatus("testSingleJobStatus");

        when(jobService.getJob(2)).thenReturn(testJob);
        ResponseEntity<JobTO> result = jobController.getJob(2);
        assertEquals(9, result.getBody().getId());
        assertEquals("testSingleJob", result.getBody().getName());
        assertEquals("testSingleJobStatus", result.getBody().getStatus());
        assertEquals(200, result.getStatusCodeValue());
        verify(jobService).getJob(2);
    }

    @Test
    public void testGetJobsByProject() {
        List<JobTO> jobs = new ArrayList<>();
        JobTO testJob = new JobTO();
        testJob.setId(11);
        testJob.setName("testJobNameProject");
        testJob.setStatus("testJobStatusProject");

        jobs.add(testJob);
        JobContainer jobContainer = new JobContainer(jobs);
        when(jobService.getJobsByProject(6)).thenReturn(jobContainer);

        ResponseEntity<JobContainer> result = jobController.getJobsByProject(6);
        assertEquals(11, result.getBody().getJobs().get(0).getId());
        assertEquals("testJobNameProject", result.getBody().getJobs().get(0).getName());
        assertEquals("testJobStatusProject", result.getBody().getJobs().get(0).getStatus());
        assertEquals(200, result.getStatusCodeValue());
        verify(jobService).getJobsByProject(6);
    }

    @Test
    public void testCreateJob() {
        Map<String, String> response = new HashMap<>();
        response.put("testJobId", "testJobStatus");
        CreateJobRequest request = new CreateJobRequest("testProject");
        when(jobService.createJob(request)).thenReturn(response);
        ResponseEntity<Map<String, String>> result = jobController.createJob(request);
        assertEquals("testJobStatus", result.getBody().get("testJobId"));
        assertEquals(201, result.getStatusCodeValue());
        assertNotNull(result.getHeaders().getLocation());
        verify(jobService).createJob(request);
    }

    @Test
    public void testGetAllJobStatus() {
        List<Map<String, String>> status = new ArrayList<>();
        Map<String, String> entry = new HashMap<>();
        entry.put("jobId", "67");
        entry.put("status", "Created");
        status.add(entry);
        when(jobService.getAllJobStatus()).thenReturn(status);
        ResponseEntity<List<Map<String, String>>> result = jobController.getAllJobStatus();
        Map<String, String> expectedEntry = result.getBody().get(0);
        assertEquals("67", expectedEntry.get("jobId"));
        assertEquals("Created", expectedEntry.get("status"));
        assertEquals(200, result.getStatusCodeValue());
        verify(jobService).getAllJobStatus();

        when(jobService.getAllJobStatus()).thenReturn(null);
        result = jobController.getAllJobStatus();
        assertEquals(404, result.getStatusCodeValue());
    }

    @Test
    public void testGetJobStatus() {
        when(jobService.getJobStatus(4)).thenReturn("Created");
        ResponseEntity<String> result = jobController.getJobStatus(4);
        assertEquals("Created", result.getBody());
        assertEquals(200, result.getStatusCodeValue());
        verify(jobService).getJobStatus(4);

        when(jobService.getJobStatus(4)).thenReturn(null);
        result = jobController.getJobStatus(4);
        assertEquals(404, result.getStatusCodeValue());
    }

    @Test
    public void testGetJobVMStatuses() {
        CloudVmStatus testStatus = new CloudVmStatus("testInstanceId", "2", "testSecurityGroup", JobStatus.Starting,
                VMImageType.AGENT, VMRegion.US_WEST_2, null, null, 0, 0, null, null);

        CloudVmStatusContainer container = new CloudVmStatusContainer();
        container.getStatuses().add(testStatus);

        when(jobService.getJobVMStatus("testJobId")).thenReturn(container);

        ResponseEntity<CloudVmStatusContainer> result = jobController.getJobVMStatuses("testJobId");
        ArrayList<CloudVmStatus> statuses = new ArrayList<>(result.getBody().getStatuses());
        CloudVmStatus expectedStatus = statuses.get(0);
        assertEquals("testInstanceId",expectedStatus.getInstanceId());
        assertEquals("2", expectedStatus.getJobId());
        assertEquals("testSecurityGroup", expectedStatus.getSecurityGroup());
        assertEquals(JobStatus.Starting, expectedStatus.getJobStatus());
        assertEquals(VMImageType.AGENT, expectedStatus.getRole());
        assertEquals(VMRegion.US_WEST_2, expectedStatus.getVmRegion());
        assertEquals(200, result.getStatusCodeValue());
        verify(jobService).getJobVMStatus("testJobId");

        when(jobService.getJobVMStatus("testJobId")).thenReturn(null);
        result = jobController.getJobVMStatuses("testJobId");
        assertEquals(404, result.getStatusCodeValue());
    }

    // Job Status Setters

    @Test
    public void testStartJob() {
        ResponseEntity<Void> result = jobController.startJob(385594786);
        assertEquals(204, result.getStatusCodeValue());
        verify(jobService).startJob(385594786);
    }

    @Test
    public void testStopJob() {
        ResponseEntity<Void> result = jobController.stopJob(808242333);
        assertEquals(204, result.getStatusCodeValue());
        verify(jobService).stopJob(808242333);
    }

    @Test
    public void testPauseJob() {
        ResponseEntity<Void> result = jobController.pauseJob(375886587);
        assertEquals(204, result.getStatusCodeValue());
        verify(jobService).pauseJob(375886587);
    }

    @Test
    public void testResumeJob() {
        ResponseEntity<Void> result = jobController.resumeJob(100403047);
        assertEquals(204, result.getStatusCodeValue());
        verify(jobService).resumeJob(100403047);
    }

    @Test
    public void testKillJob() {
        ResponseEntity<Void> result = jobController.killJob(482937134);
        assertEquals(204, result.getStatusCodeValue());
        verify(jobService).killJob(482937134);
    }
}