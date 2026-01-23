---
mode: 'agent'
description: 'Create an implementation plan for a YouTrack issue.'
model: 'elpis-planning'
---

Start planning the feature described in the YouTrack issue: ${input:issue_id:Enter YouTrack issue ID}

Before starting, make sure to gather all necessary information about the issue. 
Also be sure that you fully understand the requirements and objectives and the structure of the current project.

✅**DO**:
* Create a detailed implementation plan broken down into clear phases and steps - use the approach of creating a MD plan inside the /plan folder. Use ticket id as a part of the file name.
* Analyze the current codebase to identify areas that will be affected by the new feature.
* Identify potential challenges or dependencies that may impact the implementation.
* Ensure the plan aligns with project goals and coding standards.
* Review the plan for completeness and accuracy before finalizing.

❌**DO NOT**:
* Start coding or making changes to the codebase until the plan is fully developed and approved.
* Create any plan or summary outside of the /plan folder if not instructed otherwise.
* Overlook any aspect of the issue - ensure all requirements are addressed in the plan.
* Ignore potential risks or challenges - proactively identify and plan for them.
* Rush the planning process - take the time needed to create a thorough and effective plan.