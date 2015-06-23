<h3> Anomaly Detection in Streaming Network Data </h3>

Implements a system capable of detecting anomalies in a dynamic network environment, taking as input live streaming network-data.
The system uses machine learning techniques illustrated in the 2004 Ide & Kashima paper: <i>"Eigenspace-based Anomaly Detection in Computer Systems"</i> for the anomaly detection portion.
This allows for the system to adapt to different states of the network and thus avoid flagging false positives caused by gradual changes of states in the network depending on the 'freedom of movement' allowed in the system's configuration.

Other features implemented to allow for usage in a dynamic network environment with unknown number of hosts include:
<ul>
  <li>The system can be configured to use a set period of time to train the size of the dependency matrix.</li>
  <li>The system can independently change the network nodes represented in the dependency matrix, retaining only the most popular (decision based on user-defined threshold at each tick)</li>
  <li>A graphical representation of the network can be outputted at each tick using the included GraphStream library</li>
</ul>
