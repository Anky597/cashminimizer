import heapq
import networkx as nx
import plotly.graph_objs as go
import random
import streamlit as st

# BFS Algorithm (returns only node names, ignores weights)
def bfs(graph, start):
    visited = []
    queue = [start]
    while queue:
        node = queue.pop(0)
        if node not in visited:
            visited.append(node)
            queue.extend([neighbor if isinstance(neighbor, str) else neighbor[0] for neighbor in graph.get(node, []) if neighbor not in visited])
    return visited

# DFS Algorithm (returns only node names, ignores weights)
def dfs(graph, start, visited=None):
    if visited is None:
        visited = []
    visited.append(start)
    for neighbor in graph.get(start, []):
        if isinstance(neighbor, tuple):  # For weighted graphs, use only the node name
            neighbor = neighbor[0]
        if neighbor not in visited:
            dfs(graph, neighbor, visited)
    return visited

# Dijkstra's Algorithm (returns both node names and distances)
def dijkstra(graph, start):
    distances = {node: float('inf') for node in graph}
    distances[start] = 0
    priority_queue = [(0, start)]
    visited = []
    while priority_queue:
        current_distance, current_node = heapq.heappop(priority_queue)
        if current_node in visited:
            continue
        visited.append(current_node)
        for neighbor in graph.get(current_node, []):
            # Unpack neighbor if it's a tuple (i.e., weighted graph)
            if isinstance(neighbor, tuple):
                neighbor_node, weight = neighbor
            else:
                neighbor_node = neighbor
                weight = 1  # Default weight for unweighted edges

            distance = current_distance + weight
            if distance < distances[neighbor_node]:
                distances[neighbor_node] = distance
                heapq.heappush(priority_queue, (distance, neighbor_node))
    return visited, distances

# Visualization with animation using Plotly for 3D Graph
def plot_graph_3d_with_animation(graph, traversal, title, node_color='skyblue'):
    G = nx.Graph()

    for node, neighbors in graph.items():
        for neighbor in neighbors:
            if isinstance(neighbor, tuple):  # If it's a weighted graph, extract just the node
                neighbor = neighbor[0]
            G.add_edge(node, neighbor)

    # Generate random positions in 3D space
    pos = {node: (random.uniform(-10, 10), random.uniform(-10, 10), random.uniform(-10, 10)) for node in G.nodes()}

    # Create edge trace
    edge_trace = []
    for edge in G.edges(data=True):
        x0, y0, z0 = pos[edge[0]]
        x1, y1, z1 = pos[edge[1]]
        edge_trace.append(go.Scatter3d(x=[x0, x1, None], y=[y0, y1, None], z=[z0, z1, None],
                                       mode='lines', line=dict(color='black', width=2)))

    # Create node trace
    node_trace = go.Scatter3d(
        x=[pos[node][0] for node in G.nodes()],
        y=[pos[node][1] for node in G.nodes()],
        z=[pos[node][2] for node in G.nodes()],
        mode='markers+text',
        text=[node for node in G.nodes()],
        marker=dict(size=8, color='gray', opacity=0.8),
        textposition='top center')

    # Create animation frames for traversal
    frames = []
    for i in range(len(traversal)):
        traversal_trace = go.Scatter3d(
            x=[pos[traversal[j]][0] for j in range(i + 1)],
            y=[pos[traversal[j]][1] for j in range(i + 1)],
            z=[pos[traversal[j]][2] for j in range(i + 1)],
            mode='markers+text',
            text=[traversal[j] for j in range(i + 1)],
            marker=dict(size=8, color=node_color, opacity=0.9),
            textposition='top center')

        frames.append(go.Frame(data=[node_trace, traversal_trace] + edge_trace, name=str(i)))

    layout = go.Layout(
        title=title,
        scene=dict(xaxis=dict(showbackground=False),
                   yaxis=dict(showbackground=False),
                   zaxis=dict(showbackground=False)),
        updatemenus=[dict(type="buttons", showactive=False, buttons=[dict(label="Play", method="animate", args=[None, dict(frame=dict(duration=500, redraw=True), fromcurrent=True, mode="immediate")])])],
        showlegend=False)

    fig = go.Figure(data=[node_trace] + edge_trace, layout=layout, frames=frames)
    return fig

# Streamlit app
def main():
    st.title("Graph Traversal Comparison with Animation")

    # Input graph manually
    def input_graph_manual():
        graph = {}
        num_nodes = st.number_input("Enter number of nodes:", min_value=1, max_value=20, step=1)

        for i in range(1, num_nodes + 1):
            node = st.text_input(f"Enter node {i}:")
            neighbors = st.text_input(f"Enter neighbors of node {node} (format 'neighbor:weight' or just 'neighbor'):", key=f'neighbor_{i}')
            if neighbors:
                graph[node] = [(n.split(':')[0].strip(), float(n.split(':')[1].strip())) if ':' in n else (n.strip(), 1) for n in neighbors.split(',')]
        return graph

    # Get user input for graph
    graph = input_graph_manual()

    # If graph input is valid, proceed
    if graph:
        st.write("Graph input complete.")
        start_node = st.text_input("Enter the starting node:")

        # Select multiple algorithms for comparison
        algorithms = st.multiselect(
            "Select algorithms to compare:",
            ["BFS", "DFS", "Dijkstra"],
            default=["BFS", "DFS"]
        )

        if st.button("Run Traversals"):
            traversal_results = {}

            if "BFS" in algorithms:
                bfs_result = bfs(graph, start_node)
                traversal_results['BFS'] = bfs_result
                st.write(f"BFS Traversal: {bfs_result}")

            if "DFS" in algorithms:
                dfs_result = dfs(graph, start_node)
                traversal_results['DFS'] = dfs_result
                st.write(f"DFS Traversal: {dfs_result}")

            if "Dijkstra" in algorithms:
                dijkstra_result, distances = dijkstra(graph, start_node)
                traversal_results['Dijkstra'] = dijkstra_result
                st.write(f"Dijkstra Traversal: {dijkstra_result}")
                st.write(f"Dijkstra Distances: {distances}")

            # Show side-by-side visualizations with animation
            cols = st.columns(len(traversal_results))

            for idx, (algorithm, result) in enumerate(traversal_results.items()):
                with cols[idx]:
                    fig = plot_graph_3d_with_animation(graph, result, f"{algorithm} Traversal")
                    st.plotly_chart(fig, use_container_width=True)

if __name__ == "__main__":
    main()
