using Azure;
using Azure.Data.Tables;
using Azure.Storage.Blobs;
using Azure.Storage.Files.Shares;
using Azure.Storage.Queues;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace ABC_Retail.Pages
{
    public class SearchModel : PageModel
    {
        private readonly BlobServiceClient _blobServiceClient;
        private readonly TableServiceClient _tableServiceClient;
        private readonly ShareServiceClient _shareServiceClient;
        private readonly QueueServiceClient _queueServiceClient;

        public SearchModel(BlobServiceClient blobServiceClient, TableServiceClient tableServiceClient, ShareServiceClient shareServiceClient, QueueServiceClient queueServiceClient)
        {
            _blobServiceClient = blobServiceClient;
            _tableServiceClient = tableServiceClient;
            _shareServiceClient = shareServiceClient;
            _queueServiceClient = queueServiceClient;
        }

        // Search Results for each type of data
        public List<TableEntity> ProfileResults { get; set; } = new List<TableEntity>();
        public List<string> ImageResults { get; set; } = new List<string>();
        public List<string> ContractResults { get; set; } = new List<string>();
        public List<string> OrderResults { get; set; } = new List<string>();

        public async Task<IActionResult> OnPostSearchAsync(string searchType, string searchTerm)
        {
            if (string.IsNullOrEmpty(searchType) || string.IsNullOrEmpty(searchTerm))
            {
                ModelState.AddModelError(string.Empty, "Search term and search type are required.");
                return Page();
            }

            switch (searchType.ToLower())
            {
                case "profile":
                    await SearchProfilesAsync(searchTerm);
                    break;
                case "image":
                    SearchImagesAsync(searchTerm); // No need to await here
                    break;
                case "contract":
                    await SearchContractsAsync(searchTerm);
                    break;
                case "order":
                    await SearchOrdersAsync(searchTerm);
                    break;
                default:
                    ModelState.AddModelError(string.Empty, "Invalid search type.");
                    break;
            }

            return Page();
        }

        private async Task SearchProfilesAsync(string customerName)
        {
            var tableClient = _tableServiceClient.GetTableClient("Profiles");
            Pageable<TableEntity> queryResults = tableClient.Query<TableEntity>(entity => entity.RowKey == customerName);

            foreach (var entity in queryResults)
            {
                ProfileResults.Add(entity);
            }
        }

        private void SearchImagesAsync(string imageName)
        {
            var containerClient = _blobServiceClient.GetBlobContainerClient("images");
                var blobs = containerClient.GetBlobs();

                foreach (var blob in blobs)
                {
                    if (blob.Name.Contains(imageName)) // Adjusted to search for specific image
                    {
                        ImageResults.Add(blob.Name);
                    }
                }
            }

        private async Task SearchContractsAsync(string contractName)
        {
            var shareClient = _shareServiceClient.GetShareClient("contracts");
            var rootDir = shareClient.GetRootDirectoryClient();
            var filesAndDirectories = rootDir.GetFilesAndDirectoriesAsync();

            await foreach (var item in filesAndDirectories)
            {
                if (item.Name.Contains(contractName))
                {
                    ContractResults.Add(item.Name);
                }
            }
        }

        private async Task SearchOrdersAsync(string orderTerm)
        {
            var queueClient = _queueServiceClient.GetQueueClient("orders");
            await queueClient.CreateIfNotExistsAsync();

            var messages = await queueClient.ReceiveMessagesAsync(maxMessages: 10); // Get up to 10 messages

            foreach (var message in messages.Value)
            {
                if (message.MessageText.Contains(orderTerm))
                {
                    OrderResults.Add(message.MessageText);
                }
            }
        }
    }
}
