resource "aws_ecr_repository" "protozeus_ecr" {
  name                 = "protozeus"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false
  }
}