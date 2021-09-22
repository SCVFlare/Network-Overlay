# OVERLAY project
Two csv matrixes are provided - both the same topology but different distanes.  
There should be ONLY one Overlay running at a single time as this can result in out of synch queues.  
Hosts should preferably connect and disconnect while Overlay is running.
# To run
 - `java -jar Overlay.jar <path to csv file>`
In a new terminal connect with one or more hosts.
- `java -jar Host.jar <Virtual nodeid>`
To send messages type `left` or `right` followd by `your message` on a new line.  
Type `exit` or `Ctrl^C` to disconnect from the topology.  
# Example 1
`matrix.csv` is the same as the architecture in the report. Log in as 1,2,3,4 or 5. For example, connect as 1 and 2 and see in Overlay that message passes throug node 3.
# Example 2
`matrix2.csv` will result in a different path when sending form Node 4 to Node 5 and inversly because their distancce is set to 5, making the route through 2 faster.