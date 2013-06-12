Jmx_jenkins_plugin
==================

Gerrit test4

Plugin to view jmx output in Jenkins

**WHAT**
This plugin is based on <a href="https://github.com/jenkinsci/performance-plugin/">Performance Plugin</a> built to collect jmx data generated by <a href="https://github.com/jmxtrans/jmxtrans">jmxtrans</a> tool.  This tool hooks into a running jvm and exposes jmx data in a variety of formats.  One of those formats is <a href="https://github.com/jmxtrans/jmxtrans/wiki/KeyOutWriter">Key out writer</a>, which spits data out in a tab-delimited format.  The user must complete the following steps for the plugin to work:
<ul>
<ol>Add plugin as post-build step</ol>
<ol>Specify the path to the output file in the plugin configuration</ol>
<ol>Start java application with jmxtrans added in the startup arguments and jmxtrans json file specifying the output as KeyOutWriter</ol>
</ul>

Once the test completes, the data is published into the jenkins output.

**WHY**
Most performance-based plugins only capture response times.  There's a need to also expose internal perfmon data.  JMX is a great way to do it.

**DEPENDENCIES**
jmxtrans

**CAVEATS**
When you copy the jmx file, please note that the plugin currently reads only from master branch.  A fix for this in the works but for now, just either force builds on master or use another plugin to copy over to master server.

**ROADMAP**
<ul>
<li style="text-decoration: line-through;">Publish jmx data on build completion</li>
<li>Specify which jmx data to view in plugin</li>
<li>Allow to build on master and slave</li>
<li>Integrate with jmeter results (or performance plugin)</li>
<li>Add options to filter what jmx attributes to track</li>
<li>Add options to select which jmx attribute to view at trending (floating box)</li>
<li>Allow user to fail builds if specified attributes degraded within a threshold
	<ul>UI will include an option to set:
		<li>Checkbox to fail build</li>
		<li>List of metrics [textbox for metric] and it's threshold degradation [textbox]</li>
	</ul>
</li>
<li>Add whether to graph attribute or not</li>
<li>Ability to compare 2 builds</li>
</ul>
