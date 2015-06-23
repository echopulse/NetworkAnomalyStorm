<h3> Anomaly Detection in Streaming Network Data </h3>

Implements a system in Apache Storm capable of detecting streaming network data anomalies in a dynamic network environment.
The system uses unsupervised machine learning techniques illustrated in the 2004 Ide & Kashima paper: <i>"Eigenspace-based Anomaly Detection in Computer Systems"</i> as basis for its anomaly detection module.
This allows for the system to adapt to different states of the network at a rate defined in the program's configuration settings.

Other features implemented to allow for its use in a dynamic network environment with unknown number of nodes include:
<ul>
  <li>The system can be configured to use a set period of time to train the size of the dependency matrix.</li>
  <li>The system can independently change the network nodes represented in the dependency matrix, retaining only the most popular (decision based on user-defined threshold at each tick)</li>
  <li>A graphical representation of the network can be outputted at each tick using the included GraphStream library</li>
</ul>
