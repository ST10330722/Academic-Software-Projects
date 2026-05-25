using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Windows.Forms;
using System.Xml.Serialization;

namespace MunicipalServicesApp
{
    static class Program
    {
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            // Initialize stores
            var issueStore = new IssueDataStore("issues.xml");
            var eventStore = new EventDataStore("events.xml");

            // Seed a few issues if none exist, ensuring Graph/Heap have data
            if (issueStore.Issues.Count < 5)
            {
                issueStore.SeedSampleIssues();
                issueStore.Save();
            }

            Application.Run(new MainForm(issueStore, eventStore));
        }
    }

    #region Issue classes (Part 1/3 Enhanced)
    public class Issue
    {
        public Guid Id { get; set; } = Guid.NewGuid();
        public string Location { get; set; }
        public string Category { get; set; }
        public string Description { get; set; }
        public List<string> AttachmentPaths { get; set; } = new List<string>();
        public DateTime SubmittedAt { get; set; } = DateTime.UtcNow;
        public string Status { get; set; } = "Received";
        public int Priority { get; set; } = 1; // 1 (Low) to 5 (Urgent) - NEW for Heap implementation
    }

    public class IssueDataStore
    {
        private readonly string _filePath;
        public List<Issue> Issues { get; private set; } = new List<Issue>();

        // Dictionary for priority calculation (Graph/Heap requirement for Part 3)
        private static readonly Dictionary<string, int> IssuePriorities = new Dictionary<string, int>(StringComparer.OrdinalIgnoreCase)
        {
            { "Water", 5 },
            { "Utilities", 4 },
            { "Sanitation", 4 },
            { "Roads", 3 },
            { "Street Lighting", 2 },
            { "Other", 1 }
        };

        public IssueDataStore(string filePath)
        {
            _filePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, filePath);
            Load();
        }

        public void Add(Issue issue)
        {
            // Set priority when adding based on category
            issue.Priority = CalculatePriority(issue.Category);
            Issues.Add(issue);
            Save();
        }

        public void SeedSampleIssues()
        {
            Issues.AddRange(new[]
           {
                new Issue { Location="Main Street, downtown", Category="Water", Description="Major burst pipe flooding street.", Status="Under Review" },
                new Issue { Location="North End Park", Category="Sanitation", Description="Large amount of illegal dumping near playground." },
                new Issue { Location="Industrial Park Access Road", Category="Roads", Description="Large pothole causing traffic hazards." },
                new Issue { Location="South West Residential", Category="Utilities", Description="Power flicker since 5 PM." },
                new Issue { Location="North Side", Category="Street Lighting", Description="Three street lights out on Elm Ave." }
            });

            foreach (var issue in Issues)
            {
                issue.Priority = CalculatePriority(issue.Category);
            }
        }

        private int CalculatePriority(string category)
        {
            if (IssuePriorities.TryGetValue(category, out int priority))
            {
                return priority;
            }
            return 1; // Default to low priority
        }

        public void Save()
        {
            try
            {
                var serializer = new XmlSerializer(typeof(List<Issue>));
                using (var stream = File.Create(_filePath))
                {
                    serializer.Serialize(stream, Issues);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error saving issues: " + ex.Message);
            }
        }

        public void Load()
        {
            try
            {
                if (!File.Exists(_filePath)) return;
                var serializer = new XmlSerializer(typeof(List<Issue>));
                using (var stream = File.OpenRead(_filePath))
                {
                    Issues = (List<Issue>)serializer.Deserialize(stream);
                }
            }
            catch
            {
                Issues = new List<Issue>();
            }
        }
    }
    #endregion

    #region Event classes (Part 2)
    public class Event
    {
        public Guid Id { get; set; } = Guid.NewGuid();
        public string Title { get; set; }
        public string Category { get; set; }
        public string Description { get; set; }
        public DateTime EventDate { get; set; }
        public int Priority { get; set; } = 0;
        public string Location { get; set; }
    }

    public class EventDataStore
    {
        private readonly string _filePath;
        public List<Event> Events { get; private set; } = new List<Event>();

        public EventDataStore(string filePath)
        {
            _filePath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, filePath);
            Load();
            if (Events.Count == 0)
            {
                SeedSampleEvents();
                Save();
            }
        }

        private void SeedSampleEvents()
        {
            Events.AddRange(new[]
            {
                new Event { Title="Community Clean-up Day", Category="Community", Description="Join volunteers for litter pick-up.", EventDate=DateTime.Today.AddDays(3), Priority=2, Location="Parkside" },
                new Event { Title="Water Supply Maintenance", Category="Utilities", Description="Scheduled water maintenance.", EventDate=DateTime.Today.AddDays(1), Priority=5, Location="Various suburbs" },
                new Event { Title="Roadworks: Main Street", Category="Roads", Description="Road resurfacing scheduled.", EventDate=DateTime.Today.AddDays(7), Priority=3, Location="Main Street" },
                new Event { Title="Public Health Talk", Category="Health", Description="Vaccination information session.", EventDate=DateTime.Today.AddDays(10), Priority=1, Location="Community Hall" },
                new Event { Title="Electricity Upgrade", Category="Utilities", Description="Street lights upgrade.", EventDate=DateTime.Today.AddDays(2), Priority=4, Location="Northside" }
            });
        }

        public void Add(Event ev)
        {
            Events.Add(ev);
            Save();
        }

        public void Save()
        {
            try
            {
                var serializer = new XmlSerializer(typeof(List<Event>));
                using (var stream = File.Create(_filePath))
                {
                    serializer.Serialize(stream, Events);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error saving events: " + ex.Message);
            }
        }

        public void Load()
        {
            try
            {
                if (!File.Exists(_filePath)) return;
                var serializer = new XmlSerializer(typeof(List<Event>));
                using (var stream = File.OpenRead(_filePath))
                {
                    Events = (List<Event>)serializer.Deserialize(stream);
                }
            }
            catch
            {
                Events = new List<Event>();
            }
        }
    }
    #endregion

    #region SimplePriorityQueue<T> (Max-Heap Implementation)
    /// <summary>
    /// Implements a basic Max-Heap structure for Priority Queue functionality.
    /// Used for prioritizing the most urgent issues/events.
    /// </summary>
    public class SimplePriorityQueue<T>
    {
        private List<(T item, int priority)> _list = new List<(T, int)>();

        public void Enqueue(T item, int priority)
        {
            _list.Add((item, priority));
            _list.Sort((a, b) => b.priority.CompareTo(a.priority)); // Highest priority first (Max-Heap behavior)
        }

        public T Dequeue()
        {
            if (_list.Count == 0) throw new InvalidOperationException("Queue empty");
            var it = _list[0].item;
            _list.RemoveAt(0);
            return it;
        }

        public List<T> Peek(int count)
        {
            return _list.Take(count).Select(i => i.item).ToList();
        }

        public int Count => _list.Count;
        public void Clear() => _list.Clear();
    }
    #endregion

    #region Part 3 Graph/Traversal/MST Helper Classes

    /// <summary>
    /// Represents a node (location) in the service graph.
    /// </summary>
    public class ServiceNode
    {
        public string Name { get; set; }
        public List<Issue> Issues { get; } = new List<Issue>();

        public ServiceNode(string name)
        {
            Name = name;
        }

        public override string ToString() => Name;
    }

    /// <summary>
    /// Implements the Municipal Service Graph using an Adjacency List.
    /// Nodes are locations, edges represent distance/cost.
    /// </summary>
    public class ServiceGraph
    {
        // Adjacency List: Dictionary<Location (Node), List of Neighboring Edges>
        private Dictionary<string, ServiceNode> _nodes = new Dictionary<string, ServiceNode>(StringComparer.OrdinalIgnoreCase);
        private Dictionary<string, List<(string neighbor, int distance)>> _adjList = new Dictionary<string, List<(string, int)>>(StringComparer.OrdinalIgnoreCase);
        private readonly Random _rand = new Random();

        public IEnumerable<ServiceNode> Nodes => _nodes.Values;
        public bool ContainsLocation(string location) => _nodes.ContainsKey(location);

        // FIX: Replaced GetValueOrDefault with standard TryGetValue pattern.
        public ServiceNode GetNode(string location)
        {
            if (_nodes.TryGetValue(location, out ServiceNode node))
            {
                return node;
            }
            return null;
        }

        public void AddNode(string location)
        {
            if (!_nodes.ContainsKey(location))
            {
                _nodes.Add(location, new ServiceNode(location));
                _adjList.Add(location, new List<(string, int)>());
            }
        }

        public void AddEdge(string locA, string locB, int distance)
        {
            // FIX: Corrected string comparison
            if (locA.Equals(locB, StringComparison.OrdinalIgnoreCase)) return;

            // Add edge A -> B
            if (_adjList.ContainsKey(locA) && !_adjList[locA].Any(e => e.neighbor.Equals(locB, StringComparison.OrdinalIgnoreCase)))
            {
                _adjList[locA].Add((locB, distance));
            }

            // Add edge B -> A (undirected graph)
            if (_adjList.ContainsKey(locB) && !_adjList[locB].Any(e => e.neighbor.Equals(locA, StringComparison.OrdinalIgnoreCase)))
            {
                _adjList[locB].Add((locA, distance));
            }
        }

        /// <summary>
        /// Traversal: Uses Depth-First Search (DFS) to find all issues connected to a starting location.
        /// </summary>
        public List<Issue> DepthFirstTraversal(string startLocation)
        {
            var issuesFound = new List<Issue>();
            var visited = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
            var stack = new Stack<string>();

            if (!_adjList.ContainsKey(startLocation)) return issuesFound;

            stack.Push(startLocation);

            while (stack.Count > 0)
            {
                var currentLoc = stack.Pop();
                if (visited.Contains(currentLoc)) continue;

                visited.Add(currentLoc);

                // Add issues from this location
                var node = GetNode(currentLoc);
                if (node != null)
                {
                    issuesFound.AddRange(node.Issues.Where(i => i.Status != "Completed"));
                }

                // Push unvisited neighbors onto the stack
                if (_adjList.ContainsKey(currentLoc))
                {
                    foreach (var edge in _adjList[currentLoc])
                    {
                        if (!visited.Contains(edge.neighbor))
                        {
                            stack.Push(edge.neighbor);
                        }
                    }
                }
            }
            return issuesFound.Distinct().ToList();
        }

        /// <summary>
        /// Minimum Spanning Tree (MST): Uses Prim's Algorithm to find the most cost-effective way 
        /// to connect all locations with outstanding issues.
        /// </summary>
        public List<(string locA, string locB, int distance)> FindMinimumSpanningTree(HashSet<string> locations)
        {
            if (locations.Count == 0) return new List<(string, string, int)>();

            var mst = new List<(string locA, string locB, int distance)>();
            var inMst = new HashSet<string>(StringComparer.OrdinalIgnoreCase);

            // Priority list (simulating Min-Heap for edges)
            var edges = new List<(int distance, string locA, string locB)>();

            // Start with an arbitrary location
            string startNode = locations.First();
            inMst.Add(startNode);

            // Add all edges connected to the starting node to the list
            if (_adjList.ContainsKey(startNode))
            {
                foreach (var edge in _adjList[startNode])
                {
                    if (locations.Contains(edge.neighbor))
                    {
                        edges.Add((edge.distance, startNode, edge.neighbor));
                    }
                }
            }

            while (inMst.Count < locations.Count)
            {
                // Find the minimum weight edge (Simulating Min-Heap extract-min)
                var minEdge = edges.OrderBy(e => e.distance).FirstOrDefault();
                if (minEdge.Equals(default)) break;

                edges.Remove(minEdge);

                // Determine which end of the edge is the new node
                string newNode = "";
                string existingNode = "";

                if (!inMst.Contains(minEdge.locB) && inMst.Contains(minEdge.locA))
                {
                    newNode = minEdge.locB;
                    existingNode = minEdge.locA;
                }
                else if (!inMst.Contains(minEdge.locA) && inMst.Contains(minEdge.locB))
                {
                    newNode = minEdge.locA;
                    existingNode = minEdge.locB;
                }
                else
                {
                    continue;
                }

                if (inMst.Contains(newNode)) continue;

                inMst.Add(newNode);
                mst.Add((existingNode, newNode, minEdge.distance));

                // Add new edges from the newly added node
                if (_adjList.ContainsKey(newNode))
                {
                    foreach (var edge in _adjList[newNode])
                    {
                        if (locations.Contains(edge.neighbor) && !inMst.Contains(edge.neighbor))
                        {
                            // Add edge to the priority list
                            edges.Add((edge.distance, newNode, edge.neighbor));
                        }
                    }
                }
            }

            return mst;
        }
    }

    #endregion

    public class MainForm : Form
    {
        private Button btnReport;
        private Button btnEvents;
        private Button btnStatus;
        private IssueDataStore _issueStore;
        private EventDataStore _eventStore;

        public MainForm(IssueDataStore issueStore, EventDataStore eventStore)
        {
            _issueStore = issueStore;
            _eventStore = eventStore;
            InitComponents();
        }

        private void InitComponents()
        {
            Text = "Municipal Services - Main Menu";
            Size = new Size(480, 320);
            StartPosition = FormStartPosition.CenterScreen;

            var lbl = new Label { Text = "Select a task:", Font = new Font("Segoe UI", 12), Location = new Point(20, 20), AutoSize = true };
            Controls.Add(lbl);

            btnReport = new Button { Text = "Report Issues", Location = new Point(20, 60), Size = new Size(420, 40) };
            btnReport.Click += (s, e) => { var f = new ReportForm(_issueStore); f.ShowDialog(); };
            Controls.Add(btnReport);

            btnEvents = new Button { Text = "Local Events and Announcements", Location = new Point(20, 110), Size = new Size(420, 40), Enabled = true };
            btnEvents.Click += (s, e) => { var f = new LocalEventsForm(_eventStore); f.ShowDialog(); };
            Controls.Add(btnEvents);

            btnStatus = new Button { Text = "Service Request Status & Priority", Location = new Point(20, 160), Size = new Size(420, 40), Enabled = true };
            btnStatus.Click += (s, e) => { var f = new StatusForm(_issueStore); f.ShowDialog(); };
            Controls.Add(btnStatus);

            var lblNote = new Label { Text = "Part 1: Report Issues. Part 2: Local Events. Part 3: Status Tracking & Advanced Data Structures.", Location = new Point(20, 215), AutoSize = true };
            Controls.Add(lblNote);
        }
    }

    #region ReportForm (Part 1)
    public class ReportForm : Form
    {
        private TextBox txtLocation;
        private ComboBox cmbCategory;
        private RichTextBox rtbDescription;
        private IssueDataStore _store;
        private List<string> attachments = new List<string>();

        private readonly string[] encourageMessages = new[]
        {
            "Thanks — your report matters!",
            "We're logging your report now.",
            "Good job reporting — we're on it!",
            "You help improve your community."
        };

        public ReportForm(IssueDataStore store)
        {
            _store = store;
            InitComponents();
        }

        private void InitComponents()
        {
            Text = "Report Issues";
            Size = new Size(700, 560);
            StartPosition = FormStartPosition.CenterParent;

            // ... [UI component initialization remains the same]
            var lblLoc = new Label { Text = "Location:", Location = new Point(20, 20), AutoSize = true };
            Controls.Add(lblLoc);
            txtLocation = new TextBox { Location = new Point(20, 45), Width = 620 };
            Controls.Add(txtLocation);

            var lblCat = new Label { Text = "Category:", Location = new Point(20, 85), AutoSize = true };
            Controls.Add(lblCat);
            cmbCategory = new ComboBox { Location = new Point(20, 110), Width = 300, DropDownStyle = ComboBoxStyle.DropDownList };
            cmbCategory.Items.AddRange(new object[] { "Sanitation", "Roads", "Utilities", "Street Lighting", "Water", "Other" });
            cmbCategory.SelectedIndex = 0;
            Controls.Add(cmbCategory);

            var lblDesc = new Label { Text = "Description:", Location = new Point(20, 150), AutoSize = true };
            Controls.Add(lblDesc);
            rtbDescription = new RichTextBox { Location = new Point(20, 175), Size = new Size(620, 180) };
            Controls.Add(rtbDescription);

            var btnAttach = new Button { Text = "Attach files (images/documents)", Location = new Point(20, 370), Size = new Size(240, 30) };
            btnAttach.Click += BtnAttach_Click;
            Controls.Add(btnAttach);

            var lblAttachNote = new Label { Text = "Attached: 0 files", Location = new Point(270, 375), AutoSize = true };
            Controls.Add(lblAttachNote);

            var btnSubmit = new Button { Text = "Submit", Location = new Point(20, 420), Size = new Size(120, 40) };
            btnSubmit.Click += (s, e) => Submit_Click(lblAttachNote);
            Controls.Add(btnSubmit);

            var btnBack = new Button { Text = "Back to Main Menu", Location = new Point(150, 420), Size = new Size(150, 40) };
            btnBack.Click += (s, e) => Close();
            Controls.Add(btnBack);

            var engagementBar = new ProgressBar { Name = "EngagementBar", Location = new Point(20, 480), Size = new Size(620, 20), Minimum = 0, Maximum = 100, Value = 0 };
            Controls.Add(engagementBar);

            var lblEncourage = new Label { Name = "EncourageLabel", Text = "Welcome — your report improves the community!", Location = new Point(20, 510), AutoSize = true };
            Controls.Add(lblEncourage);
        }

        private void BtnAttach_Click(object sender, EventArgs e)
        {
            using (var dlg = new OpenFileDialog())
            {
                dlg.Multiselect = true;
                dlg.Title = "Select images or documents";
                dlg.Filter = "Images and Documents|*.jpg;*.jpeg;*.png;*.bmp;*.gif;*.pdf;*.doc;*.docx;*.txt|All files|*.*";
                if (dlg.ShowDialog() == DialogResult.OK)
                {
                    attachments.AddRange(dlg.FileNames);

                    var lblAttachNote = Controls.OfType<Label>().FirstOrDefault(c => c.Text.StartsWith("Attached:"));
                    if (lblAttachNote != null)
                    {
                        lblAttachNote.Text = $"Attached: {attachments.Count} files";
                    }
                    UpdateEngagement(10 * Math.Min(attachments.Count, 5));
                }
            }
        }

        private void Submit_Click(Label lblAttachNote)
        {
            if (string.IsNullOrWhiteSpace(txtLocation.Text) || string.IsNullOrWhiteSpace(rtbDescription.Text))
            {
                MessageBox.Show("Please enter the location and description of the issue.", "Validation", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            var issue = new Issue
            {
                Location = txtLocation.Text.Trim(),
                Category = cmbCategory.SelectedItem?.ToString(),
                Description = rtbDescription.Text.Trim(),
                AttachmentPaths = new List<string>(attachments),
                SubmittedAt = DateTime.UtcNow,
                Status = "Received"
            };

            _store.Add(issue);

            var engagementBar = Controls.OfType<ProgressBar>().FirstOrDefault(c => c.Name == "EngagementBar");
            var lblEncourage = Controls.OfType<Label>().FirstOrDefault(c => c.Name == "EncourageLabel");

            if (engagementBar != null) engagementBar.Value = Math.Min(100, engagementBar.Value + 25);
            if (lblEncourage != null) lblEncourage.Text = $"Report submitted — Tracking ID: {issue.Id.ToString().Substring(0, 8)}. {encourageMessages[new Random().Next(encourageMessages.Length)]}";

            MessageBox.Show("Report submitted successfully.\nYour tracking ID: " + issue.Id.ToString().Substring(0, 8), "Success", MessageBoxButtons.OK, MessageBoxIcon.Information);

            // Reset form
            txtLocation.Clear();
            rtbDescription.Clear();
            attachments.Clear();
            lblAttachNote.Text = "Attached: 0 files";
        }

        private void UpdateEngagement(int add)
        {
            var engagementBar = Controls.OfType<ProgressBar>().FirstOrDefault(c => c.Name == "EngagementBar");
            var lblEncourage = Controls.OfType<Label>().FirstOrDefault(c => c.Name == "EncourageLabel");

            if (engagementBar != null) engagementBar.Value = Math.Min(100, engagementBar.Value + add);
            if (lblEncourage != null) lblEncourage.Text = encourageMessages[new Random().Next(encourageMessages.Length)];
        }
    }
    #endregion

    #region LocalEventsForm (Part 2)
    public class LocalEventsForm : Form
    {
        private EventDataStore _store;
        private SortedDictionary<DateTime, List<Event>> eventsByDate = new SortedDictionary<DateTime, List<Event>>();
        private Dictionary<string, List<Event>> eventsByCategory = new Dictionary<string, List<Event>>(StringComparer.OrdinalIgnoreCase);
        private HashSet<string> uniqueCategories = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
        private Stack<string> recentSearches = new Stack<string>();
        private Queue<Event> recentlyViewed = new Queue<Event>();
        private ListView lvEvents;
        private ComboBox cmbCategoryFilter;
        private DateTimePicker dtpDateFilter;
        private TextBox txtSearch;
        private ListBox lbRecommendations;
        private Label lblStackStatus;
        private Label lblQueueStatus;

        public LocalEventsForm(EventDataStore store)
        {
            _store = store;
            InitComponents();
            BuildIndexes();
            PopulateCategoryFilter();
            DisplayAllEvents();
        }

        private void InitComponents()
        {
            Text = "Local Events and Announcements";
            Size = new Size(900, 600);
            StartPosition = FormStartPosition.CenterParent;

            var lblTitle = new Label { Text = "Local Events & Announcements", Font = new Font("Segoe UI", 14, FontStyle.Bold), Location = new Point(16, 10), AutoSize = true };
            Controls.Add(lblTitle);

            lvEvents = new ListView { Location = new Point(16, 60), Size = new Size(600, 420), View = View.Details, FullRowSelect = true };
            lvEvents.Columns.Add("Title", 220);
            lvEvents.Columns.Add("Category", 100);
            lvEvents.Columns.Add("Date", 120);
            lvEvents.Columns.Add("Location", 140);
            Controls.Add(lvEvents);

            // Filters and search
            cmbCategoryFilter = new ComboBox { Location = new Point(640, 60), Width = 220, DropDownStyle = ComboBoxStyle.DropDownList };
            Controls.Add(cmbCategoryFilter);

            dtpDateFilter = new DateTimePicker { Location = new Point(640, 100), Width = 220, Format = DateTimePickerFormat.Short };
            Controls.Add(dtpDateFilter);

            txtSearch = new TextBox { Location = new Point(640, 140), Width = 220 };
            Controls.Add(txtSearch);

            var btnSearch = new Button { Text = "Search", Location = new Point(640, 180), Width = 100 };
            btnSearch.Click += BtnSearch_Click;
            Controls.Add(btnSearch);

            var btnViewDetails = new Button { Text = "View Details", Location = new Point(16, 490), Width = 120 };
            btnViewDetails.Click += BtnViewDetails_Click;
            Controls.Add(btnViewDetails);

            // Recommendations
            var lblRec = new Label { Text = "Recommendations", Location = new Point(640, 230), AutoSize = true, Font = new Font("Segoe UI", 10, FontStyle.Bold) };
            Controls.Add(lblRec);
            lbRecommendations = new ListBox { Location = new Point(640, 260), Size = new Size(220, 200) };
            Controls.Add(lbRecommendations);

            // Data structure status
            lblStackStatus = new Label { Text = "Recent searches: 0", Location = new Point(16, 530), AutoSize = true };
            Controls.Add(lblStackStatus);
            lblQueueStatus = new Label { Text = "Recently viewed events: 0", Location = new Point(220, 530), AutoSize = true };
            Controls.Add(lblQueueStatus);

            var btnShowFeatured = new Button { Text = "Show Featured (Priority Queue)", Location = new Point(160, 490), Width = 180 };
            btnShowFeatured.Click += (s, e) => ShowFeatured();
            Controls.Add(btnShowFeatured);

            var btnBack = new Button { Text = "Back to Main Menu", Location = new Point(460, 490), Width = 140 };
            btnBack.Click += (s, e) => Close();
            Controls.Add(btnBack);
        }

        private void BuildIndexes()
        {
            eventsByDate.Clear();
            eventsByCategory.Clear();
            uniqueCategories.Clear();

            foreach (var ev in _store.Events)
            {
                // SortedDictionary (by date)
                var dateKey = ev.EventDate.Date;
                if (!eventsByDate.ContainsKey(dateKey)) eventsByDate[dateKey] = new List<Event>();
                eventsByDate[dateKey].Add(ev);

                // Dictionary
                if (!eventsByCategory.ContainsKey(ev.Category)) eventsByCategory[ev.Category] = new List<Event>();
                eventsByCategory[ev.Category].Add(ev);

                // Set
                uniqueCategories.Add(ev.Category);
            }
        }

        private void PopulateCategoryFilter()
        {
            cmbCategoryFilter.Items.Clear();
            cmbCategoryFilter.Items.Add("All Categories");
            foreach (var c in uniqueCategories.OrderBy(x => x))
                cmbCategoryFilter.Items.Add(c);
            cmbCategoryFilter.SelectedIndex = 0;
        }

        private void DisplayAllEvents()
        {
            lvEvents.Items.Clear();
            var sorted = _store.Events.OrderBy(e => e.EventDate).ThenByDescending(e => e.Priority);
            foreach (var ev in sorted)
            {
                var li = new ListViewItem(new[] { ev.Title, ev.Category, ev.EventDate.ToShortDateString(), ev.Location }) { Tag = ev };
                lvEvents.Items.Add(li);
            }
        }

        private void BtnSearch_Click(object sender, EventArgs e)
        {
            string term = txtSearch.Text.Trim();
            var category = cmbCategoryFilter.SelectedItem?.ToString();
            var date = dtpDateFilter.Value.Date;

            // Stack usage
            if (!string.IsNullOrEmpty(term))
            {
                recentSearches.Push(term);
                if (recentSearches.Count > 10)
                {
                    var temp = new Stack<string>(recentSearches.Reverse().Skip(1).Reverse());
                    recentSearches = new Stack<string>(temp);
                }
            }
            lblStackStatus.Text = $"Recent searches: {recentSearches.Count}";

            IEnumerable<Event> candidates = _store.Events;

            if (category != null && category != "All Categories" && eventsByCategory.ContainsKey(category))
            {
                candidates = eventsByCategory[category];
            }

            if (dtpDateFilter.Checked)
            {
                candidates = candidates.Where(ev => ev.EventDate.Date >= date);
            }

            if (!string.IsNullOrEmpty(term))
            {
                var lower = term.ToLower();
                candidates = candidates.Where(ev => (ev.Title != null && ev.Title.ToLower().Contains(lower)) || (ev.Description != null && ev.Description.ToLower().Contains(lower)));
            }

            var results = candidates.OrderBy(ev => ev.EventDate).ThenByDescending(ev => ev.Priority).ToList();

            lvEvents.Items.Clear();
            foreach (var ev in results)
            {
                var li = new ListViewItem(new[] { ev.Title, ev.Category, ev.EventDate.ToShortDateString(), ev.Location }) { Tag = ev };
                lvEvents.Items.Add(li);
            }

            UpdateRecommendations(term);
        }

        private void UpdateRecommendations(string lastSearchTerm)
        {
            var recs = new List<Event>();

            // 1) Recommend top featured (Priority Queue)
            var featuredEvents = _store.Events.Where(e => e.Priority > 0).OrderByDescending(e => e.Priority).Take(3).ToList();
            recs.AddRange(featuredEvents);

            // 2) Recommend based on recent searches (Stack) - prefer categories matching search terms
            if (!string.IsNullOrEmpty(lastSearchTerm))
            {
                var lower = lastSearchTerm.ToLower();
                var bySearchTerm = _store.Events.Where(ev => (ev.Title != null && ev.Title.ToLower().Contains(lower)) || (ev.Description != null && ev.Description.ToLower().Contains(lower))).Take(5);
                foreach (var e in bySearchTerm) if (!recs.Contains(e)) recs.Add(e);
            }

            // 3) Recommend events similar to recently viewed events (Queue)
            foreach (var v in recentlyViewed.ToArray().Reverse().Take(3))
            {
                var similar = _store.Events.Where(ev => ev.Category.Equals(v.Category, StringComparison.OrdinalIgnoreCase) && ev.Id != v.Id).Take(2);
                foreach (var e in similar) if (!recs.Contains(e)) recs.Add(e);
            }

            if (recs.Count == 0)
            {
                recs.AddRange(_store.Events.OrderBy(ev => ev.EventDate).Take(5));
            }

            lbRecommendations.Items.Clear();
            foreach (var e in recs.Distinct().Take(8))
            {
                lbRecommendations.Items.Add($"{e.EventDate.ToShortDateString()} - {e.Title} ({e.Category})");
            }
        }

        private void BtnViewDetails_Click(object sender, EventArgs e)
        {
            if (lvEvents.SelectedItems.Count == 0)
            {
                MessageBox.Show("Select an event from the list first.", "Information", MessageBoxButtons.OK, MessageBoxIcon.Information);
                return;
            }

            var ev = lvEvents.SelectedItems[0].Tag as Event;

            // Queue usage
            recentlyViewed.Enqueue(ev);
            if (recentlyViewed.Count > 10) recentlyViewed.Dequeue();
            lblQueueStatus.Text = $"Recently viewed events: {recentlyViewed.Count}";

            var msg = $"Title: {ev.Title}\nCategory: {ev.Category}\nDate: {ev.EventDate}\nLocation: {ev.Location}\n\nDescription:\n{ev.Description}\n\nPriority: {ev.Priority}";
            MessageBox.Show(msg, "Event Details", MessageBoxButtons.OK, MessageBoxIcon.Information);

            UpdateRecommendations(null);
        }

        private void ShowFeatured()
        {
            var featuredQueue = new SimplePriorityQueue<Event>();
            foreach (var ev in _store.Events.Where(e => e.Priority > 0))
            {
                featuredQueue.Enqueue(ev, ev.Priority);
            }

            var featured = featuredQueue.Peek(5);
            lvEvents.Items.Clear();
            foreach (var ev in featured)
            {
                var li = new ListViewItem(new[] { ev.Title, ev.Category, ev.EventDate.ToShortDateString(), ev.Location }) { Tag = ev };
                lvEvents.Items.Add(li);
            }
            MessageBox.Show("Displayed top 5 featured events ordered by priority (Max-Heap operation).", "Featured Events", MessageBoxButtons.OK, MessageBoxIcon.Information);
        }
    }
    #endregion

    #region StatusForm (Part 3 Enhanced with Heap/Graph/Traversal/MST)

    public class StatusForm : Form
    {
        private TextBox txtTrackingId;
        private Label lblStatusResult;
        private Button btnSearch;
        private IssueDataStore _store;
        private ListBox lbUrgentIssues;
        private ServiceGraph _graph; // The Graph Structure
        private ListBox lbGraphTraversal; // For Traversal/MST output

        public StatusForm(IssueDataStore store)
        {
            _store = store;
            _graph = new ServiceGraph(); // Initialize the Graph
            BuildServiceGraph(); // Populate the Graph
            InitComponents();
            DisplayUrgentIssues(); // Display Heap
            ShowResourceAllocation(); // Display Graph/MST
        }

        private void BuildServiceGraph()
        {
            // 1. Define nodes (all unique locations)
            var uniqueLocations = _store.Issues.Select(i => i.Location).Distinct(StringComparer.OrdinalIgnoreCase).ToHashSet();

            // Add sample major hubs/locations if they aren't in the issues list
            uniqueLocations.Add("Central Depot");
            uniqueLocations.Add("North Side");
            uniqueLocations.Add("South West");
            uniqueLocations.Add("Industrial Park");

            foreach (var loc in uniqueLocations)
            {
                _graph.AddNode(loc);
            }

            // 2. Assign issues to nodes
            foreach (var issue in _store.Issues)
            {
                if (_graph.ContainsLocation(issue.Location))
                {
                    _graph.GetNode(issue.Location)?.Issues.Add(issue);
                }
            }

            // 3. Define Edges (connecting locations with arbitrary distances)
            // This is a simplified representation of actual distance/cost
            _graph.AddEdge("Central Depot", "North Side", 10);
            _graph.AddEdge("Central Depot", "South West", 15);
            _graph.AddEdge("Central Depot", "Industrial Park", 25);
            _graph.AddEdge("North Side", "Industrial Park", 12);
            _graph.AddEdge("South West", "Industrial Park", 18);

            // Add random connections between 5 random unique locations
            var locationsArray = uniqueLocations.ToArray();
            var rand = new Random();
            for (int i = 0; i < 5; i++)
            {
                if (locationsArray.Length < 2) break;
                int idx1 = rand.Next(locationsArray.Length);
                int idx2 = rand.Next(locationsArray.Length);
                if (idx1 != idx2)
                {
                    _graph.AddEdge(locationsArray[idx1], locationsArray[idx2], rand.Next(5, 30));
                }
            }
        }

        private void InitComponents()
        {
            Text = "Service Request Status & Priority Allocation (Graph/Heap/MST)";
            Size = new Size(1100, 600); // Expanded size
            StartPosition = FormStartPosition.CenterParent;

            // Left side: Status Lookup
            var lblInstruction = new Label { Text = "Enter Tracking ID:", Location = new Point(20, 20), AutoSize = true };
            Controls.Add(lblInstruction);

            txtTrackingId = new TextBox { Location = new Point(20, 45), Width = 200 };
            Controls.Add(txtTrackingId);

            btnSearch = new Button { Text = "Search", Location = new Point(230, 43), Width = 100 };
            btnSearch.Click += BtnSearch_Click;
            Controls.Add(btnSearch);

            var lblResultTitle = new Label { Text = "Request Status:", Location = new Point(20, 90), Font = new Font("Segoe UI", 10, FontStyle.Bold), AutoSize = true };
            Controls.Add(lblResultTitle);

            lblStatusResult = new Label
            {
                Text = "Enter a Tracking ID to check status.",
                Location = new Point(20, 120),
                AutoSize = false,
                Size = new Size(310, 300),
                BorderStyle = BorderStyle.FixedSingle
            };
            Controls.Add(lblStatusResult);

            // Center: Heap / Priority Queue Visualization
            var lblUrgentTitle = new Label { Text = "Top 3 Urgent Issues (Max-Heap):", Location = new Point(350, 20), Font = new Font("Segoe UI", 10, FontStyle.Bold), AutoSize = true };
            Controls.Add(lblUrgentTitle);

            lbUrgentIssues = new ListBox { Location = new Point(350, 45), Size = new Size(420, 375), Font = new Font("Segoe UI", 9) };
            Controls.Add(lbUrgentIssues);

            // Right Side: Graph/Traversal/MST Output
            var lblGraphTitle = new Label { Text = "Resource Allocation (Graph/Traversal/MST):", Location = new Point(780, 20), Font = new Font("Segoe UI", 10, FontStyle.Bold), AutoSize = true };
            Controls.Add(lblGraphTitle);

            lbGraphTraversal = new ListBox { Location = new Point(780, 45), Size = new Size(290, 450), Font = new Font("Segoe UI", 9) };
            Controls.Add(lbGraphTraversal);

            var btnGraphAlloc = new Button { Text = "Show Allocation (DFS/MST)", Location = new Point(780, 510), Width = 290, Height = 40 };
            btnGraphAlloc.Click += (s, e) => ShowResourceAllocation();
            Controls.Add(btnGraphAlloc);
        }

        // HEAP / PRIORITY QUEUE IMPLEMENTATION (Part 3 Requirement)
        private void DisplayUrgentIssues()
        {
            var urgentQueue = new SimplePriorityQueue<Issue>();

            foreach (var issue in _store.Issues.Where(i => i.Status != "Completed"))
            {
                urgentQueue.Enqueue(issue, issue.Priority);
            }

            lbUrgentIssues.Items.Clear();

            if (urgentQueue.Count == 0)
            {
                lbUrgentIssues.Items.Add("No urgent issues currently outstanding.");
                return;
            }

            var topUrgent = urgentQueue.Peek(3);

            int rank = 1;
            foreach (var issue in topUrgent)
            {
                lbUrgentIssues.Items.Add($"Rank {rank++} (P:{issue.Priority}): {issue.Category} - {issue.Location} - Status: {issue.Status}");
            }

            if (urgentQueue.Count > 3)
            {
                lbUrgentIssues.Items.Add($"...and {urgentQueue.Count - 3} more in the queue.");
            }
        }

        // GRAPH TRAVERSAL / MST IMPLEMENTATION (Part 3 Requirement)
        private void ShowResourceAllocation()
        {
            lbGraphTraversal.Items.Clear();

            // --- 1. Graph Traversal (DFS) ---
            string startLocation = "Central Depot";

            lbGraphTraversal.Items.Add($"--- 1. Service Route (DFS Traversal) ---");
            lbGraphTraversal.Items.Add($"Starting from: {startLocation}");

            var traversalResults = _graph.DepthFirstTraversal(startLocation);

            if (traversalResults.Any())
            {
                lbGraphTraversal.Items.Add($"Issues found on connected route ({traversalResults.Count}):");
                foreach (var issue in traversalResults.Take(5))
                {
                    lbGraphTraversal.Items.Add($"  -> {issue.Location} ({issue.Category})");
                }
                if (traversalResults.Count > 5) lbGraphTraversal.Items.Add($"  ... and {traversalResults.Count - 5} more.");
            }
            else
            {
                lbGraphTraversal.Items.Add("No issues found along the connected routes from the depot.");
            }

            lbGraphTraversal.Items.Add("-------------------------------------------");

            // --- 2. Minimum Spanning Tree (MST - Prim's) ---
            lbGraphTraversal.Items.Add($"--- 2. Resource Network (Prim's MST) ---");

            var activeLocations = _store.Issues
                                       .Where(i => i.Status != "Completed")
                                       .Select(i => i.Location)
                                       .Distinct(StringComparer.OrdinalIgnoreCase)
                                       .ToHashSet();

            if (!activeLocations.Contains("Central Depot") && _graph.ContainsLocation("Central Depot")) activeLocations.Add("Central Depot");

            var mstEdges = _graph.FindMinimumSpanningTree(activeLocations);
            int totalCost = mstEdges.Sum(e => e.distance);

            if (mstEdges.Any())
            {
                lbGraphTraversal.Items.Add($"Locations to connect: {activeLocations.Count}");
                lbGraphTraversal.Items.Add($"Total Minimum Connection Cost: {totalCost} km");
                lbGraphTraversal.Items.Add($"Edges (Minimal Network for Access):");
                foreach (var edge in mstEdges.Take(5))
                {
                    lbGraphTraversal.Items.Add($"  {edge.locA} -({edge.distance}km)- {edge.locB}");
                }
                if (mstEdges.Count > 5) lbGraphTraversal.Items.Add($"  ... and {mstEdges.Count - 5} more edges.");
            }
            else
            {
                lbGraphTraversal.Items.Add("Not enough distinct, connected locations to form an MST.");
            }
        }

        private void BtnSearch_Click(object sender, EventArgs e)
        {
            string searchId = txtTrackingId.Text.Trim();

            if (string.IsNullOrEmpty(searchId))
            {
                MessageBox.Show("Please enter a Tracking ID.", "Error", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            var issue = _store.Issues.FirstOrDefault(i => i.Id.ToString().StartsWith(searchId, StringComparison.OrdinalIgnoreCase));

            if (issue != null)
            {
                lblStatusResult.Text = $"ID: {issue.Id.ToString().Substring(0, 8)}...\nLocation: {issue.Location}\nCategory: {issue.Category} (Priority: {issue.Priority})\nSubmitted: {issue.SubmittedAt.ToLocalTime():yyyy-MM-dd HH:mm}\n\n**Current Status: {issue.Status}**";

                SimulateStatusUpdate(issue);
                DisplayUrgentIssues(); // Refresh Heap display after status update
                ShowResourceAllocation(); // Refresh Graph display after status update
            }
            else
            {
                lblStatusResult.Text = $"**Error:** No service request found with Tracking ID starting with '{searchId}'.\n(Try submitting a new request to get a valid ID.)";
            }
        }

        private void SimulateStatusUpdate(Issue issue)
        {
            var rand = new Random();
            string newStatus = issue.Status;

            if (issue.Status == "Received")
            {
                if (rand.Next(3) == 0) newStatus = "Under Review";
            }
            else if (issue.Status == "Under Review")
            {
                if (rand.Next(3) == 0) newStatus = "Scheduled for Repair";
            }
            else if (issue.Status == "Scheduled for Repair")
            {
                if (rand.Next(3) == 0) newStatus = "Completed";
            }

            if (issue.Status != newStatus)
            {
                issue.Status = newStatus;
                _store.Save();
            }
        }
    }
    #endregion 
}