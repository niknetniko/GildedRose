{
  description = "Gilded rose";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    devshell = {
      url = "github:numtide/devshell";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { self, nixpkgs, devshell, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { 
          inherit system;
          overlays = [ devshell.overlays.default ];
          config.allowUnfree = true;
        };
        java = pkgs.jdk21;
      in
      {
        devShells = rec {
          default = gilded-rose;
          gilded-rose = pkgs.devshell.mkShell {
            name = "gilded-rose";
            packages = [
              java pkgs.git pkgs.python3
            ];
            env = [
              {
                name = "JAVA_HOME";
                eval = java.home;
              }
            ];
          };
        };
      }
    );
}
