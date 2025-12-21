# CareerMate Job Application & Interview Scheduling - State Diagram

## Complete Workflow State Diagram (DrawIO XML Format)

```xml
<mxfile host="app.diagrams.net">
  <diagram id="JobApplicationWorkflow" name="Job Application & Interview Workflow">
    <mxGraphModel dx="1422" dy="794" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1169" pageHeight="1654" math="0" shadow="0">
      <root>
        <mxCell id="0" />
        <mxCell id="1" parent="0" />
        
        <!-- Title -->
        <mxCell id="title" value="CareerMate Job Application &amp; Interview State Diagram" style="text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=20;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="280" y="20" width="600" height="40" as="geometry" />
        </mxCell>

        <!-- START STATE -->
        <mxCell id="start" value="" style="ellipse;whiteSpace=wrap;html=1;aspect=fixed;fillColor=#000000;" vertex="1" parent="1">
          <mxGeometry x="520" y="80" width="40" height="40" as="geometry" />
        </mxCell>

        <!-- JOB APPLICATION STATES -->
        
        <!-- SUBMITTED -->
        <mxCell id="SUBMITTED" value="SUBMITTED" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="460" y="160" width="160" height="60" as="geometry" />
        </mxCell>
        
        <!-- REVIEWING -->
        <mxCell id="REVIEWING" value="REVIEWING" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="460" y="270" width="160" height="60" as="geometry" />
        </mxCell>
        
        <!-- INTERVIEW_SCHEDULED -->
        <mxCell id="INTERVIEW_SCHEDULED" value="INTERVIEW_SCHEDULED" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="460" y="380" width="160" height="60" as="geometry" />
        </mxCell>
        
        <!-- INTERVIEWED -->
        <mxCell id="INTERVIEWED" value="INTERVIEWED" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#e1d5e7;strokeColor=#9673a6;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="460" y="490" width="160" height="60" as="geometry" />
        </mxCell>
        
        <!-- APPROVED -->
        <mxCell id="APPROVED" value="APPROVED" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="460" y="600" width="160" height="60" as="geometry" />
        </mxCell>
        
        <!-- OFFER_EXTENDED -->
        <mxCell id="OFFER_EXTENDED" value="OFFER_EXTENDED" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="460" y="710" width="160" height="60" as="geometry" />
        </mxCell>
        
        <!-- WORKING -->
        <mxCell id="WORKING" value="WORKING" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;fontStyle=1;fontSize=14" vertex="1" parent="1">
          <mxGeometry x="460" y="820" width="160" height="60" as="geometry" />
        </mxCell>
        
        <!-- TERMINATED -->
        <mxCell id="TERMINATED" value="TERMINATED" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#f8cecc;strokeColor=#b85450;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="460" y="930" width="160" height="60" as="geometry" />
        </mxCell>
        
        <!-- REJECTED -->
        <mxCell id="REJECTED" value="REJECTED" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#f8cecc;strokeColor=#b85450;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="720" y="490" width="160" height="60" as="geometry" />
        </mxCell>
        
        <!-- WITHDRAWN -->
        <mxCell id="WITHDRAWN" value="WITHDRAWN" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#e1d5e7;strokeColor=#9673a6;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="200" y="490" width="160" height="60" as="geometry" />
        </mxCell>
        
        <!-- BANNED -->
        <mxCell id="BANNED" value="BANNED" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#000000;strokeColor=#000000;fontColor=#FFFFFF;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="720" y="600" width="160" height="60" as="geometry" />
        </mxCell>
        
        <!-- NO_RESPONSE -->
        <mxCell id="NO_RESPONSE" value="NO_RESPONSE" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#666666;fontStyle=1" vertex="1" parent="1">
          <mxGeometry x="720" y="270" width="160" height="60" as="geometry" />
        </mxCell>

        <!-- INTERVIEW SUB-STATES (Nested States) -->
        <mxCell id="interview_container" value="Interview Sub-States" style="swimlane;fontStyle=1;childLayout=stackLayout;horizontal=1;startSize=30;horizontalStack=0;resizeParent=1;resizeParentMax=0;resizeLast=0;collapsible=1;marginBottom=0;fillColor=#f5f5f5;strokeColor=#666666;" vertex="1" parent="1">
          <mxGeometry x="50" y="710" width="280" height="280" as="geometry" />
        </mxCell>
        
        <mxCell id="SCHEDULED" value="SCHEDULED (Initial)" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;" vertex="1" parent="interview_container">
          <mxGeometry x="20" y="40" width="240" height="40" as="geometry" />
        </mxCell>
        
        <mxCell id="CONFIRMED" value="CONFIRMED (Candidate Accepted)" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;" vertex="1" parent="interview_container">
          <mxGeometry x="20" y="90" width="240" height="40" as="geometry" />
        </mxCell>
        
        <mxCell id="RESCHEDULED" value="RESCHEDULED (Needs 2nd Round)" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;" vertex="1" parent="interview_container">
          <mxGeometry x="20" y="140" width="240" height="40" as="geometry" />
        </mxCell>
        
        <mxCell id="COMPLETED" value="COMPLETED (Interview Done)" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;" vertex="1" parent="interview_container">
          <mxGeometry x="20" y="190" width="240" height="40" as="geometry" />
        </mxCell>
        
        <mxCell id="CANCELLED" value="CANCELLED (By Recruiter/Candidate)" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#f8cecc;strokeColor=#b85450;" vertex="1" parent="interview_container">
          <mxGeometry x="20" y="240" width="240" height="40" as="geometry" />
        </mxCell>

        <!-- INTERVIEW OUTCOMES (Diamond States) -->
        <mxCell id="outcome_container" value="Interview Outcomes" style="swimlane;fontStyle=1;childLayout=stackLayout;horizontal=1;startSize=30;horizontalStack=0;resizeParent=1;resizeParentMax=0;resizeLast=0;collapsible=1;marginBottom=0;fillColor=#e1d5e7;strokeColor=#9673a6;" vertex="1" parent="1">
          <mxGeometry x="920" y="710" width="220" height="180" as="geometry" />
        </mxCell>
        
        <mxCell id="PASS" value="PASS → APPROVED" style="text;strokeColor=#82b366;fillColor=#d5e8d4;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;" vertex="1" parent="outcome_container">
          <mxGeometry y="30" width="220" height="30" as="geometry" />
        </mxCell>
        
        <mxCell id="FAIL" value="FAIL → REJECTED" style="text;strokeColor=#b85450;fillColor=#f8cecc;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;" vertex="1" parent="outcome_container">
          <mxGeometry y="60" width="220" height="30" as="geometry" />
        </mxCell>
        
        <mxCell id="PENDING" value="PENDING → INTERVIEWED" style="text;strokeColor=#9673a6;fillColor=#e1d5e7;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;" vertex="1" parent="outcome_container">
          <mxGeometry y="90" width="220" height="30" as="geometry" />
        </mxCell>
        
        <mxCell id="NEEDS_SECOND_ROUND" value="NEEDS_SECOND_ROUND → REVIEWING" style="text;strokeColor=#d6b656;fillColor=#fff2cc;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;" vertex="1" parent="outcome_container">
          <mxGeometry y="120" width="220" height="30" as="geometry" />
        </mxCell>

        <!-- ARROWS / TRANSITIONS -->
        
        <!-- Start to SUBMITTED -->
        <mxCell id="edge1" value="Candidate Applies" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0.5;exitY=1;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;" edge="1" parent="1" source="start" target="SUBMITTED">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>
        
        <!-- SUBMITTED to REVIEWING -->
        <mxCell id="edge2" value="Recruiter Reviews" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0.5;exitY=1;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;" edge="1" parent="1" source="SUBMITTED" target="REVIEWING">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>
        
        <!-- REVIEWING to INTERVIEW_SCHEDULED -->
        <mxCell id="edge3" value="Interview Scheduled" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0.5;exitY=1;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;" edge="1" parent="1" source="REVIEWING" target="INTERVIEW_SCHEDULED">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>
        
        <!-- INTERVIEW_SCHEDULED to INTERVIEWED -->
        <mxCell id="edge4" value="Interview Completed" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0.5;exitY=1;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;" edge="1" parent="1" source="INTERVIEW_SCHEDULED" target="INTERVIEWED">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>
        
        <!-- INTERVIEWED to APPROVED -->
        <mxCell id="edge5" value="Interview PASS" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0.5;exitY=1;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;strokeColor=#82b366;strokeWidth=2;" edge="1" parent="1" source="INTERVIEWED" target="APPROVED">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>
        
        <!-- APPROVED to OFFER_EXTENDED -->
        <mxCell id="edge6" value="Offer Sent" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0.5;exitY=1;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;" edge="1" parent="1" source="APPROVED" target="OFFER_EXTENDED">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>
        
        <!-- OFFER_EXTENDED to WORKING -->
        <mxCell id="edge7" value="Candidate Accepts Offer" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0.5;exitY=1;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;strokeColor=#82b366;strokeWidth=3;" edge="1" parent="1" source="OFFER_EXTENDED" target="WORKING">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>
        
        <!-- WORKING to TERMINATED -->
        <mxCell id="edge8" value="Employment Ends" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0.5;exitY=1;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;strokeColor=#b85450;strokeWidth=2;" edge="1" parent="1" source="WORKING" target="TERMINATED">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>
        
        <!-- INTERVIEWED to REJECTED (FAIL) -->
        <mxCell id="edge9" value="Interview FAIL" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=1;exitY=0.5;exitDx=0;exitDy=0;entryX=0;entryY=0.5;entryDx=0;entryDy=0;strokeColor=#b85450;strokeWidth=2;" edge="1" parent="1" source="INTERVIEWED" target="REJECTED">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>
        
        <!-- INTERVIEWED to REVIEWING (NEEDS_SECOND_ROUND) -->
        <mxCell id="edge10" value="Needs 2nd Round" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0;exitY=0.5;exitDx=0;exitDy=0;entryX=0;entryY=0.5;entryDx=0;entryDy=0;strokeColor=#d6b656;strokeWidth=2;curved=1;" edge="1" parent="1" source="INTERVIEWED" target="REVIEWING">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="400" y="520" />
              <mxPoint x="400" y="300" />
            </Array>
          </mxGeometry>
        </mxCell>
        
        <!-- REVIEWING to REJECTED (Direct Rejection) -->
        <mxCell id="edge11" value="Reject Without Interview" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=1;exitY=0.5;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;strokeColor=#b85450;strokeWidth=1;dashed=1;" edge="1" parent="1" source="REVIEWING" target="REJECTED">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="800" y="300" />
            </Array>
          </mxGeometry>
        </mxCell>
        
        <!-- SUBMITTED to NO_RESPONSE -->
        <mxCell id="edge12" value="7+ Days No Response" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=1;exitY=0.5;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;strokeColor=#666666;strokeWidth=1;dashed=1;" edge="1" parent="1" source="SUBMITTED" target="NO_RESPONSE">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="800" y="190" />
            </Array>
          </mxGeometry>
        </mxCell>
        
        <!-- Any State to WITHDRAWN -->
        <mxCell id="edge13" value="Candidate Withdraws" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0;exitY=0.5;exitDx=0;exitDy=0;entryX=1;entryY=0.5;entryDx=0;entryDy=0;strokeColor=#9673a6;strokeWidth=1;dashed=1;" edge="1" parent="1" source="SUBMITTED" target="WITHDRAWN">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="400" y="190" />
              <mxPoint x="400" y="520" />
            </Array>
          </mxGeometry>
        </mxCell>
        
        <mxCell id="edge14" value="Candidate Withdraws" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0;exitY=0.5;exitDx=0;exitDy=0;entryX=0.5;entryY=0;entryDx=0;entryDy=0;strokeColor=#9673a6;strokeWidth=1;dashed=1;" edge="1" parent="1" source="REVIEWING" target="WITHDRAWN">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="280" y="300" />
            </Array>
          </mxGeometry>
        </mxCell>
        
        <mxCell id="edge15" value="Candidate Withdraws" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0;exitY=0.5;exitDx=0;exitDy=0;entryX=0.5;entryY=1;entryDx=0;entryDy=0;strokeColor=#9673a6;strokeWidth=1;dashed=1;" edge="1" parent="1" source="INTERVIEW_SCHEDULED" target="WITHDRAWN">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="280" y="410" />
            </Array>
          </mxGeometry>
        </mxCell>
        
        <!-- OFFER_EXTENDED to WITHDRAWN (Decline Offer) -->
        <mxCell id="edge16" value="Candidate Declines Offer" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0;exitY=0.5;exitDx=0;exitDy=0;entryX=0;entryY=0.5;entryDx=0;entryDy=0;strokeColor=#9673a6;strokeWidth=2;curved=1;" edge="1" parent="1" source="OFFER_EXTENDED" target="WITHDRAWN">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="180" y="740" />
              <mxPoint x="180" y="520" />
            </Array>
          </mxGeometry>
        </mxCell>
        
        <!-- Admin BANNED -->
        <mxCell id="edge17" value="Admin Bans Candidate" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=1;exitY=0.5;exitDx=0;exitDy=0;entryX=0;entryY=0.5;entryDx=0;entryDy=0;strokeColor=#000000;strokeWidth=2;dashed=1;" edge="1" parent="1" source="REJECTED" target="BANNED">
          <mxGeometry relative="1" as="geometry">
            <Array as="points">
              <mxPoint x="800" y="550" />
              <mxPoint x="800" y="630" />
            </Array>
          </mxGeometry>
        </mxCell>
        
        <!-- Interview Sub-State Connection -->
        <mxCell id="edge18" value="Interview Process Details" style="edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=0;exitY=0.5;exitDx=0;exitDy=0;entryX=1;entryY=0.5;entryDx=0;entryDy=0;strokeColor=#6c8ebf;strokeWidth=1;dashed=1;" edge="1" parent="1" source="INTERVIEW_SCHEDULED" target="SCHEDULED">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>

        <!-- Legend -->
        <mxCell id="legend" value="Legend" style="swimlane;fontStyle=1;childLayout=stackLayout;horizontal=1;startSize=30;horizontalStack=0;resizeParent=1;resizeParentMax=0;resizeLast=0;collapsible=1;marginBottom=0;fillColor=#f5f5f5;strokeColor=#666666;" vertex="1" parent="1">
          <mxGeometry x="920" y="80" width="220" height="240" as="geometry" />
        </mxCell>
        
        <mxCell id="legend1" value="Green = Success/Active States" style="text;strokeColor=#82b366;fillColor=#d5e8d4;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;" vertex="1" parent="legend">
          <mxGeometry y="30" width="220" height="30" as="geometry" />
        </mxCell>
        
        <mxCell id="legend2" value="Yellow = In-Progress States" style="text;strokeColor=#d6b656;fillColor=#fff2cc;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;" vertex="1" parent="legend">
          <mxGeometry y="60" width="220" height="30" as="geometry" />
        </mxCell>
        
        <mxCell id="legend3" value="Blue = Scheduled/Waiting" style="text;strokeColor=#6c8ebf;fillColor=#dae8fc;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;" vertex="1" parent="legend">
          <mxGeometry y="90" width="220" height="30" as="geometry" />
        </mxCell>
        
        <mxCell id="legend4" value="Purple = Completed/Neutral" style="text;strokeColor=#9673a6;fillColor=#e1d5e7;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;" vertex="1" parent="legend">
          <mxGeometry y="120" width="220" height="30" as="geometry" />
        </mxCell>
        
        <mxCell id="legend5" value="Red = Rejected/Failed States" style="text;strokeColor=#b85450;fillColor=#f8cecc;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;" vertex="1" parent="legend">
          <mxGeometry y="150" width="220" height="30" as="geometry" />
        </mxCell>
        
        <mxCell id="legend6" value="Gray = No Response" style="text;strokeColor=#666666;fillColor=#f5f5f5;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;" vertex="1" parent="legend">
          <mxGeometry y="180" width="220" height="30" as="geometry" />
        </mxCell>
        
        <mxCell id="legend7" value="Black = Banned (Admin Action)" style="text;strokeColor=#000000;fillColor=#000000;fontColor=#FFFFFF;align=left;verticalAlign=middle;spacingLeft=4;spacingRight=4;overflow=hidden;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;rotatable=0;" vertex="1" parent="legend">
          <mxGeometry y="210" width="220" height="30" as="geometry" />
        </mxCell>

      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
```

## State Transition Rules Summary

### Job Application States (13 Total)

| State | Description | Entry Conditions | Exit Transitions |
|-------|-------------|-----------------|------------------|
| **SUBMITTED** | Initial application | Candidate applies to job | → REVIEWING (recruiter reviews)<br/>→ NO_RESPONSE (7+ days)<br/>→ WITHDRAWN (candidate cancels) |
| **REVIEWING** | Under recruiter review | From SUBMITTED or NEEDS_SECOND_ROUND | → INTERVIEW_SCHEDULED (schedule interview)<br/>→ REJECTED (no interview)<br/>→ WITHDRAWN (candidate cancels) |
| **INTERVIEW_SCHEDULED** | Interview booked | Interview scheduled | → INTERVIEWED (interview complete)<br/>→ WITHDRAWN (candidate cancels) |
| **INTERVIEWED** | Interview completed | Interview marked complete | → APPROVED (outcome: PASS)<br/>→ REJECTED (outcome: FAIL)<br/>→ REVIEWING (outcome: NEEDS_SECOND_ROUND)<br/>→ INTERVIEWED (outcome: PENDING) |
| **APPROVED** | Passed interview | Interview outcome: PASS | → OFFER_EXTENDED (offer sent) |
| **OFFER_EXTENDED** | Job offer sent | Recruiter extends offer | → WORKING (candidate accepts)<br/>→ WITHDRAWN (candidate declines) |
| **WORKING** | Currently employed | Candidate starts work | → TERMINATED (employment ends) |
| **TERMINATED** | Employment ended | Employment terminated | **Terminal State** |
| **REJECTED** | Application rejected | Interview FAIL or direct reject | → BANNED (admin bans) |
| **WITHDRAWN** | Candidate withdrew | Candidate withdraws anytime | **Terminal State** |
| **BANNED** | Candidate banned | Admin action | **Terminal State** |
| **NO_RESPONSE** | No company response | 7+ days no recruiter action | **Terminal State** |
| **ACCEPTED** | Legacy state | (v2.0 - deprecated) | Use WORKING instead |

### Interview Sub-States (6 Total)

| State | Description | Triggers | Next State |
|-------|-------------|----------|-----------|
| **SCHEDULED** | Interview created | `scheduleInterview()` | → CONFIRMED (candidate accepts)<br/>→ CANCELLED (cancelled)<br/>→ RESCHEDULED (needs 2nd round) |
| **CONFIRMED** | Candidate confirmed | `confirmInterview()` | → COMPLETED (interview happens)<br/>→ CANCELLED (cancelled)<br/>→ NO_SHOW (candidate doesn't attend) |
| **RESCHEDULED** | Needs rescheduling | Outcome: NEEDS_SECOND_ROUND | → SCHEDULED (new interview created) |
| **COMPLETED** | Interview done | `completeInterview()` | **Terminal** (outcome determines JobApply state) |
| **CANCELLED** | Interview cancelled | `cancelInterview()` | **Terminal** |
| **NO_SHOW** | Candidate no-show | `markNoShow()` | **Terminal** (JobApply → REJECTED) |

### Interview Outcomes (4 Total)

| Outcome | JobApply Transition | Description |
|---------|---------------------|-------------|
| **PASS** | → APPROVED | Candidate passed, ready for offer |
| **FAIL** | → REJECTED | Candidate failed, reject application |
| **PENDING** | → INTERVIEWED | Still deciding, keep in INTERVIEWED |
| **NEEDS_SECOND_ROUND** | → REVIEWING | Schedule another interview round |

## Key Business Rules

1. **Multi-Round Interviews**: System supports `interviewRound` (1, 2, 3...) - multiple interviews per application
2. **Candidate Confirmation**: Interview status changes SCHEDULED → CONFIRMED when candidate accepts
3. **7-Day No Response**: Auto-transition to NO_RESPONSE if recruiter doesn't act on SUBMITTED
4. **Withdrawal Anytime**: Candidates can withdraw from any non-terminal state
5. **Admin Ban Authority**: Admins can ban candidates from REJECTED state
6. **Employment Lifecycle**: WORKING → TERMINATED when employment ends
7. **Offer Acceptance**: OFFER_EXTENDED → WORKING or WITHDRAWN (binary choice)
8. **Interview Blocking**: Cannot schedule new interview if SCHEDULED/CONFIRMED interview exists
9. **No-Show Penalty**: NO_SHOW interview → JobApply REJECTED
10. **Second Round Loop**: NEEDS_SECOND_ROUND → Interview RESCHEDULED → JobApply REVIEWING → schedule new interview

## Implementation Notes

- **StatusJobApply** enum has 13 states (12 active + 1 deprecated)
- **InterviewStatus** enum has 6 states for interview lifecycle
- **InterviewOutcome** enum has 4 values determining post-interview flow
- **State persistence**: PostgreSQL with JPA/Hibernate
- **Notifications**: Kafka events trigger on all state transitions
- **Version**: v3.0 introduced WORKING/TERMINATED, v3.1 added OFFER_EXTENDED
