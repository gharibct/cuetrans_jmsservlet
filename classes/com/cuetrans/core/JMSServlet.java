package com.cuetrans.core;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JMSServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
        System.out.println("\n\n**********Inside JMSServlet");
    TrafficLogger.log("\n" + "\nINCOMING >>>  Workflow: \n" + request.getParameter("workFlowName") + " \n Params: \n"
        + request.getParameter("workFlowParams") + "\n");
    try {
      ActionHandlerNew.actionHandle(request, response);
    } catch (Exception e) {
      TrafficLogger.log("ERROR !!! " + e.getMessage());
      e.printStackTrace();
    }
  }
}