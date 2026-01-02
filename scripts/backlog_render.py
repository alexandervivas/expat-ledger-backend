import json
import os

def render_backlog():
    backlog_dir = 'docs/backlog'
    output_file = 'BACKLOG.md'

    if not os.path.exists(backlog_dir):
        print(f"Directory {backlog_dir} not found.")
        return

    iterations = []
    for filename in sorted(os.listdir(backlog_dir)):
        if filename.endswith('.json') and filename.startswith('iteration-'):
            with open(os.path.join(backlog_dir, filename), 'r') as f:
                iterations.append(json.load(f))

    with open(output_file, 'w') as f:
        f.write("# Project Backlog\n\n")
        for it in iterations:
            status_emoji = "✅" if it['status'] == 'completed' else "🚀" if it['status'] == 'in-progress' else "⏳"
            f.write(f"## Iteration {it['iteration']}: {it['name']} {status_emoji}\n\n")
            f.write("| ID | Task | Status | Owner |\n")
            f.write("| :--- | :--- | :--- | :--- |\n")
            for task in it['tasks']:
                task_status = "✅ Done" if task['status'] == 'done' else "🚧 In Progress" if task['status'] == 'in-progress' else "⏳ Todo"
                f.write(f"| {task['id']} | {task['title']} | {task_status} | {task['owner']} |\n")
            f.write("\n")

if __name__ == "__main__":
    render_backlog()
    print("BACKLOG.md rendered successfully.")
